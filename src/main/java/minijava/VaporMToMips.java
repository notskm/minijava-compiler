package minijava;

import cs132.vapor.ast.VDataSegment;
import cs132.vapor.ast.VOperand;
import cs132.vapor.ast.VaporProgram;

public class VaporMToMips {
    private int indentLevel;

    public void toMips(VaporProgram vapor) {
        String dataSegment = compileDataSegments(vapor);
        System.out.println(dataSegment);
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

        dataSegment += toLine("");
        return dataSegment;
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
