package minijava;

import java.util.Map;
import java.util.HashMap;

import cs132.vapor.ast.VAddr;
import cs132.vapor.ast.VAssign;
import cs132.vapor.ast.VBranch;
import cs132.vapor.ast.VBuiltIn;
import cs132.vapor.ast.VCall;
import cs132.vapor.ast.VCodeLabel;
import cs132.vapor.ast.VDataSegment;
import cs132.vapor.ast.VFunction;
import cs132.vapor.ast.VGoto;
import cs132.vapor.ast.VInstr;
import cs132.vapor.ast.VLabelRef;
import cs132.vapor.ast.VLitInt;
import cs132.vapor.ast.VLitStr;
import cs132.vapor.ast.VOperand;
import cs132.vapor.ast.VReturn;
import cs132.vapor.ast.VVarRef;
import cs132.vapor.ast.VaporProgram;
import cs132.vapor.ast.VInstr.VisitorPR;
import cs132.vapor.ast.VMemRead;
import cs132.vapor.ast.VMemRef;
import cs132.vapor.ast.VMemWrite;

public class VaporMToMips {
    private int indentLevel;
    private StaticData data = new StaticData();

    public String toMips(VaporProgram vapor) {
        final String dataSegment = compileDataSegments(vapor);
        final String textSegment = compileTextSegment(vapor);
        final String builtinFunctions = getBuiltinFunctions();
        final String endDataSegment = getEndDataSegment();

        final String program = dataSegment + textSegment + builtinFunctions + endDataSegment;
        return program;
    }

    private String compileDataSegments(VaporProgram vapor) {
        String dataSegment = ".data\n\n";

        for (VDataSegment segment : vapor.dataSegments) {
            dataSegment += toLine(segment.ident + ":");

            indentLevel++;

            for (VOperand operand : segment.values) {
                if (operand instanceof VLabelRef) {
                    dataSegment += toLine(operand.toString().substring(1));
                } else {
                    dataSegment += toLine(operand.toString());
                }
            }
            indentLevel--;
            dataSegment += toLine("");
        }

        return dataSegment;
    }

    private String compileTextSegment(VaporProgram vapor) {
        String textSegment = ".text\n\n";
        indentLevel++;
        textSegment += toLine("jal Main");
        textSegment += toLine("li $v0 10");
        textSegment += toLine("syscall");
        indentLevel--;

        textSegment += toLine("");

        textSegment += compileFunctions(vapor);

        return textSegment;
    }

    private String compileFunctions(VaporProgram vapor) {
        String functions = "";

        for (VFunction function : vapor.functions) {
            functions += compileFunction(function);
            functions += toLine("");
        }

        return functions;
    }

    static final int wordSize = 4;
    int frameSize = 0;
    static final int frameOffset = -8;

    private String compileFunction(VFunction function) {
        frameSize = (2 + function.stack.local + function.stack.out) * wordSize;

        String func = "";
        func += function.ident + ":\n";
        indentLevel++;
        func += functionPrologue(function);
        func += compileFunctionBody(function);
        func += functionEpilogue(function);
        indentLevel--;

        return func;
    }

    private String functionPrologue(VFunction function) {
        String prologue = "";
        prologue += toLine("sw $fp -8($sp)");
        prologue += toLine("move $fp $sp");
        prologue += toLine("subu $sp $sp " + frameSize);
        prologue += toLine("sw $ra -4($fp)");
        return prologue;
    }

    private String compileFunctionBody(VFunction function) {
        String body = "";

        Map<Integer, String> labelIndex = buildLabelIndex(function);

        InstructionVis instructionVis = new InstructionVis(frameSize);

        int i = 0;
        for (VInstr instruction : function.body) {
            try {
                body += labelIndex.getOrDefault(i, "");
                body += instruction.accept(data, instructionVis);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            i++;
        }

        return body;
    }

    private Map<Integer, String> buildLabelIndex(VFunction function) {
        Map<Integer, String> labelIndex = new HashMap<>();
        for (VCodeLabel label : function.labels) {
            String l = labelIndex.getOrDefault(label.instrIndex, "");
            l += label.ident + ":\n";
            labelIndex.put(label.instrIndex, l);
        }
        return labelIndex;
    }

    private String functionEpilogue(VFunction function) {
        String epilogue = "";
        epilogue += toLine("lw $ra -4($fp)");
        epilogue += toLine("lw $fp -8($fp)");
        epilogue += toLine("addu $sp $sp " + frameSize);
        epilogue += toLine("jr $ra");
        return epilogue;
    }

    private String getBuiltinFunctions() {
        String builtins = getPrintFunction();
        builtins += toLine("");
        builtins += getErrorFunction();
        builtins += toLine("");
        builtins += getHeapAllocFunction();
        builtins += toLine("");
        return builtins;
    }

    private String getPrintFunction() {
        String print = toLine("_print:");

        indentLevel++;
        print += toLine("move $t9 $v0");
        print += toLine("li $v0 1   # syscall: print integer");
        print += toLine("syscall");
        print += toLine("la $a0 _newline");
        print += toLine("li $v0 4   # syscall: print string");
        print += toLine("syscall");
        print += toLine("move $v0 $t9");
        print += toLine("jr $ra");
        indentLevel--;

        return print;
    }

    private String getErrorFunction() {
        String error = toLine("_error:");

        indentLevel++;
        error += toLine("li $v0 4   # syscall: print string");
        error += toLine("syscall");
        error += toLine("li $v0 10  # syscall: exit");
        error += toLine("syscall");
        indentLevel--;

        return error;
    }

    private String getHeapAllocFunction() {
        String heapAlloc = toLine("_heapAlloc:");

        indentLevel++;
        heapAlloc += toLine("li $v0 9   # syscall: sbrk");
        heapAlloc += toLine("syscall");
        heapAlloc += toLine("jr $ra");
        indentLevel--;

        return heapAlloc;
    }

    private String getEndDataSegment() {
        String dataSegment = toLine(".data");
        dataSegment += toLine(".align 0");
        dataSegment += toLine("_newline: .asciiz \"\\n\"");
        dataSegment += compileStringLiterals();
        return dataSegment;
    }

    private String compileStringLiterals() {
        String literals = "";
        for (Map.Entry<String, String> entry : data.strings.entrySet()) {
            final String name = entry.getValue();
            final String literal = entry.getKey();
            final String litWithNewLine = literal.substring(0, literal.length() - 1) + "\\n\"";
            literals += toLine(name + ": .asciiz " + litWithNewLine);
        }
        return literals;
    }

    private String toLine(String line) {
        return indent(line) + "\n";
    }

    private String indent(String str) {
        String newStr = str;
        for (int i = 0; i < indentLevel * 2; i++) {
            newStr = " " + newStr;
        }

        return newStr;
    }

    static class StaticData {
        public Map<String, String> strings = new HashMap<>();
    }

    static class InstructionVis extends VisitorPR<StaticData, String, Throwable> {
        final int frameSize;

        public InstructionVis(int frameSize) {
            this.frameSize = frameSize;
        }

        private String toLine(String str) {
            return "  " + str + "\n";
        }

        @Override
        public String visit(StaticData data, VAssign arg0) throws Throwable {
            String mnemonic = "move";

            if (arg0.source instanceof VLitInt) {
                mnemonic = "li";
            } else if (arg0.source instanceof VLabelRef) {
                String subprogram = toLine("la $t9 " + arg0.source.toString().split(":")[1]);
                subprogram += toLine("move " + arg0.dest + " $t9");
                return subprogram;
            }

            return toLine(mnemonic + " " + arg0.dest + " " + arg0.source);
        }

        @Override
        public String visit(StaticData data, VCall arg0) throws Throwable {
            if (arg0.addr instanceof VAddr.Label) {
                final String addr = arg0.addr.toString().split(":")[1];
                return toLine("jal " + addr);
            } else {
                return toLine("jalr " + arg0.addr);
            }
        }

        @Override
        public String visit(StaticData data, VBuiltIn arg0) throws Throwable {
            switch (arg0.op.name) {
                case "Add":
                    return addOp(data, arg0);
                case "Sub":
                    return subOp(data, arg0);
                case "MulS":
                    return mulOp(data, arg0);
                case "LtS":
                    return compareOp(data, arg0);
                case "Lt":
                    return compareOpUnsigned(data, arg0);
                default:
                    return builtinFunction(data, arg0);
            }
        }

        private String addOp(StaticData data, VBuiltIn arg0) {
            final String mnemonic = arg0.args[1] instanceof VLitInt ? "addi" : "add";
            return binOp(mnemonic, data, arg0);
        }

        private String subOp(StaticData data, VBuiltIn arg0) {
            final String mnemonic = arg0.args[1] instanceof VLitInt ? "subi" : "sub";
            return binOp(mnemonic, data, arg0);
        }

        private String mulOp(StaticData data, VBuiltIn arg0) {
            return binOp("mul", data, arg0);
        }

        private String compareOp(StaticData data, VBuiltIn arg0) {
            final String mnemonic = arg0.args[1] instanceof VLitInt ? "slti" : "slt";
            return binOp(mnemonic, data, arg0);
        }

        private String compareOpUnsigned(StaticData data, VBuiltIn arg0) {
            final String mnemonic = arg0.args[1] instanceof VLitInt ? "sltiu" : "sltu";
            return binOp(mnemonic, data, arg0);
        }

        private String binOp(String mnemonic, StaticData data, VBuiltIn arg0) {
            String subprogram = "";

            String lhs = arg0.args[0].toString();
            String rhs = arg0.args[1].toString();

            if (arg0.args[0] instanceof VLitInt) {
                if (lhs.equals("0")) {
                    lhs = "$0";
                } else {
                    subprogram += toLine("li $t9 " + lhs);
                    lhs = "$t9";
                }
            }
            subprogram += toLine(mnemonic + " " + arg0.dest.toString() + " " + lhs + " " + rhs);

            return subprogram;
        }

        private String storeStringLiteralAndGetReference(StaticData data, String literal) {
            final String newRef = "_str" + data.strings.size();
            final String ref = data.strings.putIfAbsent(literal, newRef);
            return ref == null ? newRef : ref;
        }

        private String builtinFunction(StaticData data, VBuiltIn arg0) {
            String subprogram = "";

            int i = 0;
            for (VOperand operand : arg0.args) {
                String mnemonic = "";
                String argString = operand.toString();
                if (operand instanceof VLitInt) {
                    mnemonic = "li";
                } else if (operand instanceof VLitStr) {
                    mnemonic = "la";
                    argString = storeStringLiteralAndGetReference(data, operand.toString());
                    argString = data.strings.get(operand.toString());
                } else {
                    mnemonic = "move";
                }
                subprogram += toLine(mnemonic + " $a" + i + " " + argString);
                i++;
            }

            String jumpMnemonic = "jal";

            String builtinName = "";
            if (arg0.op.name.equals("HeapAllocZ")) {
                builtinName = "_heapAlloc";
            } else if (arg0.op.name.equals("Error")) {
                builtinName = "_error";
                jumpMnemonic = "j";
            } else if (arg0.op.name.equals("PrintIntS")) {
                builtinName = "_print";
            }

            subprogram += toLine(jumpMnemonic + " " + builtinName);

            if (arg0.dest != null) {
                subprogram += toLine("move " + arg0.dest.toString() + " $v0");
            }

            return subprogram;
        }

        @Override
        public String visit(StaticData data, VMemWrite arg0) throws Throwable {
            if (arg0.dest instanceof VMemRef.Global) {
                return storeToGlobal(data, arg0, (VMemRef.Global) arg0.dest);
            } else {
                return storeToStack(data, arg0, (VMemRef.Stack) arg0.dest);
            }
        }

        private String storeToGlobal(StaticData data, VMemWrite arg0, VMemRef.Global dest) {
            String subprogram = "";

            final int byteOffset = dest.byteOffset;
            final String pointer = byteOffset + "(" + dest.base + ")";

            if (arg0.source instanceof VVarRef) {
                subprogram += toLine("sw " + arg0.source.toString() + " " + pointer);
            } else if (arg0.source instanceof VLabelRef) {
                final String label = arg0.source.toString().substring(1);
                subprogram += toLine("la $t9 " + label);
                subprogram += toLine("sw $t9 " + pointer);
            } else {
                String val = arg0.source.toString();
                if (val.equals("0")) {
                    subprogram += toLine("sw $0" + " " + pointer);
                } else {
                    subprogram += toLine("li $t9 " + val);
                    subprogram += toLine("sw $t9 " + pointer);
                }
            }

            return subprogram;
        }

        private String storeToStack(StaticData data, VMemWrite arg0, VMemRef.Stack dest) {
            String subprogram = "";

            final String source = arg0.source.toString();

            final String stackPointer = getFrameLocation(dest.index, dest);

            if (arg0.source instanceof VVarRef) {
                subprogram += toLine("sw " + source + " " + stackPointer);
            } else if (arg0.source instanceof VLabelRef) {
                subprogram += toLine("la $t9 " + source.split(":")[1]);
                subprogram += toLine("sw $t9 " + stackPointer);
            } else {
                subprogram += toLine("li $t9 " + source);
                subprogram += toLine("sw $t9 " + stackPointer);
            }
            return subprogram;
        }

        private String getFrameLocation(int index, VMemRef.Stack stack) {
            if (stack.region == VMemRef.Stack.Region.Local) {
                return getLocalFrameLocation(index, stack);
            } else if (stack.region == VMemRef.Stack.Region.In) {
                return getInFrameLocation(index, stack);
            } else {
                return getOutFrameLocation(index, stack);
            }
        }

        private String getLocalFrameLocation(int index, VMemRef.Stack stack) {
            final int offset = frameSize + frameOffset - (wordSize + index * wordSize);
            return offset + "($sp)";
        }

        private String getInFrameLocation(int index, VMemRef.Stack stack) {
            final int offset = index * wordSize;
            return offset + "($fp)";
        }

        private String getOutFrameLocation(int index, VMemRef.Stack stack) {
            final int offset = index * wordSize;
            return offset + "($sp)";
        }

        @Override
        public String visit(StaticData data, VMemRead arg0) throws Throwable {
            if (arg0.source instanceof VMemRef.Global) {
                return loadFromGlobal(data, arg0, (VMemRef.Global) arg0.source);
            } else {
                return loadFromStack(data, arg0, (VMemRef.Stack) arg0.source);
            }
        }

        private String loadFromGlobal(StaticData data, VMemRead arg0, VMemRef.Global global) {
            String subprogram = "";

            final int byteOffset = global.byteOffset;
            String source = global.base.toString();
            if (global.base instanceof VAddr.Label) {
                subprogram += toLine("la $t9 " + source.split(":")[1]);
                source = "$t9";
            }
            subprogram += toLine("lw " + arg0.dest + " " + byteOffset + "(" + source + ")");
            return subprogram;
        }

        private String loadFromStack(StaticData data, VMemRead arg0, VMemRef.Stack stack) {
            final String pointer = getFrameLocation(stack.index, stack);
            return toLine("lw " + arg0.dest + " " + pointer);
        }

        @Override
        public String visit(StaticData data, VBranch arg0) throws Throwable {
            String subprogram = "";

            String arg = arg0.value.toString();
            if (arg0.value instanceof VVarRef) {
                arg = arg0.value.toString();
            } else {
                arg = "$t9";
                subprogram += toLine("li $t9 " + arg0.value);
            }

            final String mnemonic = arg0.positive ? "bnez" : "beqz";
            subprogram += toLine(mnemonic + " " + arg + " " + arg0.target.ident);
            return subprogram;
        }

        @Override
        public String visit(StaticData data, VGoto arg0) throws Throwable {
            return toLine("j " + arg0.target.toString().substring(1));
        }

        @Override
        public String visit(StaticData data, VReturn arg0) throws Throwable {
            return "";
        }
    }
}
