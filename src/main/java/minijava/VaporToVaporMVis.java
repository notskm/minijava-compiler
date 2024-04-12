package minijava;

import cs132.vapor.ast.*;
import cs132.vapor.ast.VFunction.Stack;

public class VaporToVaporMVis {
    int indentLevel = 0;

    public void toVaporM(VaporProgram vapor) {
        String dataSegment = compileDataSegments(vapor);
        String functions = compileFunctions(vapor);
        System.out.println(dataSegment + functions);
    }

    private String compileDataSegments(VaporProgram vapor) {
        String dataSegment = "";

        for (VDataSegment segment : vapor.dataSegments) {
            dataSegment += toLine("const " + segment.ident);

            indentLevel++;

            for (VOperand operand : segment.values) {
                dataSegment += toLine(operand.toString());
            }

            indentLevel--;
        }

        dataSegment += toLine("");
        return dataSegment;
    }

    private String compileFunctions(VaporProgram vapor) {
        String functions = "";

        for (VFunction function : vapor.functions) {
            Stack stack = function.stack;
            functions += toLine(
                    "func " + function.ident + " [in " + stack.in + ", out " + stack.out + ", local " + stack.local
                            + "]");
        }

        return functions;
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
