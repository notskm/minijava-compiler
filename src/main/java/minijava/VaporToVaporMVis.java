package minijava;

import java.util.Map;
import java.util.HashMap;

import cs132.vapor.ast.*;

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
            SpillEverywhere allocator = new SpillEverywhere(function);

            functions += toLine(
                    "func " + function.ident + " [in " + allocator.inStackSize + ", out " + allocator.outStackSize
                            + ", local "
                            + allocator.stackSize
                            + "]");

            Map<Integer, String> labelIndex = new HashMap<>();
            for (VCodeLabel label : function.labels) {
                String l = labelIndex.getOrDefault(label.instrIndex, "");
                l += label.ident + ":\n";
                labelIndex.put(label.instrIndex, l);
            }

            for (Map.Entry<String, String> pair : allocator.calleeMap.entrySet()) {
                final String paramReg = pair.getKey();
                final String saveReg = pair.getValue();

                functions += "  " + allocator.localMap.get(saveReg) + " = " + saveReg + "\n";
                functions += "  " + saveReg + " = " + paramReg + "\n";
            }

            int i = 0;
            InstructionVis vis = new InstructionVis();
            for (VInstr instruction : function.body) {
                try {
                    functions += labelIndex.getOrDefault(i, "");

                    final String result = instruction.accept(allocator, vis);
                    functions += result;
                } catch (Exception e) {
                }
                i++;
            }
            functions += "\n";
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
