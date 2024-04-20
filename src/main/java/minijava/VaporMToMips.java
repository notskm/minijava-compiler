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
import cs132.vapor.ast.VOperand;
import cs132.vapor.ast.VReturn;
import cs132.vapor.ast.VaporProgram;
import cs132.vapor.ast.VInstr.VisitorR;
import cs132.vapor.ast.VMemRead;
import cs132.vapor.ast.VMemWrite;
import cs132.vapor.ast.VMemRef.Global;

public class VaporMToMips {
    private int indentLevel;

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
                body += instruction.accept(instructionVis);
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

    static class InstructionVis extends VisitorR<String, Throwable> {
        private String toLine(String str) {
            return "  " + str + "\n";
        }

        @Override
        public String visit(VAssign arg0) throws Throwable {
            String mnemonic = "move";

            if (arg0.source instanceof VLitInt) {
                mnemonic = "li";
            }

            return toLine(mnemonic + " " + arg0.dest + " " + arg0.source);
        }

        @Override
        public String visit(VCall arg0) throws Throwable {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'visit'");
        }

        @Override
        public String visit(VBuiltIn arg0) throws Throwable {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'visit'");
        }

        @Override
        public String visit(VMemWrite arg0) throws Throwable {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'visit'");
        }

        @Override
        public String visit(VMemRead arg0) throws Throwable {
            final Global source = (Global) (arg0.source);
            final int byteOffset = source.byteOffset;
            return toLine("lw " + arg0.dest + " " + byteOffset + "(" + source.base.toString() + ")");
        }

        @Override
        public String visit(VBranch arg0) throws Throwable {
            final String mnemonic = arg0.positive ? "bnez" : "beqz";
            return toLine(mnemonic + " " + arg0.value + " " + arg0.target.ident);
        }

        @Override
        public String visit(VGoto arg0) throws Throwable {
            return toLine("j " + arg0.target.toString().substring(1));
        }

        @Override
        public String visit(VReturn arg0) throws Throwable {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'visit'");
        }

    }
}
