package minijava;

import java.util.Map;
import java.util.HashMap;

import cs132.vapor.ast.VAssign;
import cs132.vapor.ast.VBranch;
import cs132.vapor.ast.VBuiltIn;
import cs132.vapor.ast.VCall;
import cs132.vapor.ast.VCodeLabel;
import cs132.vapor.ast.VDataSegment;
import cs132.vapor.ast.VFunction;
import cs132.vapor.ast.VGoto;
import cs132.vapor.ast.VInstr;
import cs132.vapor.ast.VLitInt;
import cs132.vapor.ast.VLitStr;
import cs132.vapor.ast.VOperand;
import cs132.vapor.ast.VReturn;
import cs132.vapor.ast.VaporProgram;
import cs132.vapor.ast.VInstr.VisitorPR;
import cs132.vapor.ast.VMemRead;
import cs132.vapor.ast.VMemWrite;
import cs132.vapor.ast.VMemRef.Global;

public class VaporMToMips {
    private int indentLevel;
    private StaticData data = new StaticData();

    public void toMips(VaporProgram vapor) {
        String dataSegment = compileDataSegments(vapor);
        String textSegment = compileTextSegment(vapor);
        System.out.println(dataSegment + textSegment);
    }

    private String compileDataSegments(VaporProgram vapor) {
        String dataSegment = ".data\n\n";

        for (VDataSegment segment : vapor.dataSegments) {
            dataSegment += toLine(segment.ident + ":");

            indentLevel++;

            for (VOperand operand : segment.values) {
                dataSegment += toLine(operand.toString().substring(1));
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

    private String compileFunction(VFunction function) {
        String func = "";
        func += function.ident + ":\n";
        indentLevel++;
        func += functionPrologue();
        func += compileFunctionBody(function);
        func += functionEpilogue();
        indentLevel--;

        return func;
    }

    private String functionPrologue() {
        String prologue = "";
        prologue += toLine("sw $fp -8($sp)");
        prologue += toLine("move $fp $sp");
        prologue += toLine("subu $sp $sp 8");
        prologue += toLine("sw $ra -4($fp)");
        return prologue;
    }

    private String compileFunctionBody(VFunction function) {
        String body = "";

        Map<Integer, String> labelIndex = buildLabelIndex(function);

        InstructionVis instructionVis = new InstructionVis();

        int i = 0;
        for (VInstr instruction : function.body) {
            try {
                body += labelIndex.getOrDefault(i, "");
                body += instruction.accept(data, instructionVis);
            } catch (Throwable e) {

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

    private String functionEpilogue() {
        String epilogue = "";
        epilogue += toLine("lw $ra -4($fp)");
        epilogue += toLine("lw $fp -8($fp)");
        epilogue += toLine("addu $sp $sp 8");
        epilogue += toLine("jr $ra");
        return epilogue;
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
        private String toLine(String str) {
            return "  " + str + "\n";
        }

        @Override
        public String visit(StaticData data, VAssign arg0) throws Throwable {
            String mnemonic = "move";

            if (arg0.source instanceof VLitInt) {
                mnemonic = "li";
            }

            return toLine(mnemonic + " " + arg0.dest + " " + arg0.source);
        }

        @Override
        public String visit(StaticData data, VCall arg0) throws Throwable {
            return toLine("jalr " + arg0.addr);
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
                default:
                    return builtinFunction(data, arg0);
            }
        }

        private String addOp(StaticData data, VBuiltIn arg0) {
            return binOp("addu", data, arg0);
        }

        private String subOp(StaticData data, VBuiltIn arg0) {
            return binOp("subu", data, arg0);
        }

        private String mulOp(StaticData data, VBuiltIn arg0) {
            return binOp("mul", data, arg0);
        }

        private String compareOp(StaticData data, VBuiltIn arg0) {
            return binOp("slti", data, arg0);
        }

        private String binOp(String mnemonic, StaticData data, VBuiltIn arg0) {
            String subprogram = "";

            String lhs = arg0.args[0].toString();
            String rhs = arg0.args[1].toString();

            if (arg0.args[0] instanceof VLitInt) {
                subprogram += toLine("li $t9 " + lhs);
                lhs = "$t9";
            }
            subprogram += toLine(mnemonic + " " + arg0.dest.toString() + " " + lhs + " " + rhs);

            return subprogram;
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
                    argString = data.strings.get(operand.toString());
                    if (argString == null) {
                        argString = "_str" + data.strings.size();
                        data.strings.put(operand.toString(), argString);
                    }
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
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'visit'");
        }

        @Override
        public String visit(StaticData data, VMemRead arg0) throws Throwable {
            final Global source = (Global) (arg0.source);
            final int byteOffset = source.byteOffset;
            return toLine("lw " + arg0.dest + " " + byteOffset + "(" + source.base.toString() + ")");
        }

        @Override
        public String visit(StaticData data, VBranch arg0) throws Throwable {
            final String mnemonic = arg0.positive ? "bnez" : "beqz";
            return toLine(mnemonic + " " + arg0.value + " " + arg0.target.ident);
        }

        @Override
        public String visit(StaticData data, VGoto arg0) throws Throwable {
            return toLine("j " + arg0.target.toString().substring(1));
        }

        @Override
        public String visit(StaticData data, VReturn arg0) throws Throwable {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'visit'");
        }
    }
}
