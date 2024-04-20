package minijava;

import cs132.vapor.ast.VDataSegment;
import cs132.vapor.ast.VFunction;
import cs132.vapor.ast.VOperand;
import cs132.vapor.ast.VaporProgram;

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
}
