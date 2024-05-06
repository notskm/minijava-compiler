package minijava;

import java.util.Map;
import java.util.HashMap;

import cs132.vapor.ast.*;

public class VaporToVaporMVis {
    int indentLevel = 0;

    public String toVaporM(VaporProgram vapor) {
        String dataSegment = compileDataSegments(vapor);
        String functions = compileFunctions(vapor);
        return dataSegment + functions;
    }

    private String compileDataSegments(VaporProgram vapor) {
        String dataSegment = "";

        for (VDataSegment segment : vapor.dataSegments) {
            dataSegment += compileDataSegment(segment);
            dataSegment += toLine("");
        }

        return dataSegment;
    }

    private String compileDataSegment(VDataSegment segment) {
        String dataSegment = toLine("const " + segment.ident);

        indentLevel++;

        for (VOperand operand : segment.values) {
            dataSegment += toLine(operand.toString());
        }

        indentLevel--;

        return dataSegment;
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
        String body = "";
        LinearScan allocator = new LinearScan(function);

        Map<Integer, String> labelIndex = createLabelIndex(function);

        indentLevel++;

        String backupCallees = "";
        String restoreCallees = "";
        for (int i = 0; i < allocator.calleeRegisters.size(); i++) {
            final String local = "local[" + i + "]";
            final String reg = allocator.calleeRegisters.get(i);
            backupCallees += toLine(local + " = " + reg);
            restoreCallees += toLine(reg + " = " + local);
        }

        body += backupCallees;

        int nextA = 0;
        int nextIn = 0;
        for (VVarRef.Local param : function.params) {
            String reg = allocator.location.get(param.ident);
            if (reg == null) {
                reg = allocator.variableRegisters.get(param.ident);
            }

            // Some params are unused, don't need a register.
            if (reg != null) {
                if (nextA < 4) {
                    body += toLine(reg + " = $a" + nextA);
                } else {
                    body += toLine(reg + " = in[" + nextIn + "]");
                    nextIn++;
                }
            }
            nextA++;
        }

        int i = 0;
        InstructionVis2 vis = new InstructionVis2();
        for (VInstr instruction : function.body) {
            try {
                body += labelIndex.getOrDefault(i, "");

                final String result = instruction.accept(allocator, vis);
                body += result;
            } catch (Exception e) {

            }
            i++;
        }

        body += restoreCallees;
        body += toLine("ret");

        indentLevel--;

        int in = Math.max(0, function.params.length - 4);
        int out = vis.outStackSize;
        int local = allocator.location.size() + allocator.calleeRegisters.size();
        body = toLine(
                "func " + function.ident + " [in " + in + ", out " + out + ", local " + local + "]") + body;

        return body;
    }

    private Map<Integer, String> createLabelIndex(VFunction function) {
        Map<Integer, String> labelIndex = new HashMap<>();
        for (VCodeLabel label : function.labels) {
            String l = labelIndex.getOrDefault(label.instrIndex, "");
            l += label.ident + ":\n";
            labelIndex.put(label.instrIndex, l);
        }

        return labelIndex;
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

    public class InstructionVis2 extends VInstr.VisitorPR<LinearScan, String, Exception> {
        public int outStackSize = 0;

        @Override
        public String visit(LinearScan alloc, VAssign arg0) throws Exception {
            if (!alloc.aliveAt(arg0.dest.toString(), arg0.sourcePos.line)) {
                return "";
            }

            String subprogram = "";

            final String dest = getStorage(arg0.dest.toString(), alloc);

            String source = arg0.source.toString();
            if (arg0.source instanceof VVarRef) {
                final String sourceLocation = getStorage(arg0.source.toString(), alloc);
                if (sourceLocation.startsWith("local") && dest.startsWith("local")) {
                    source = "$v1";
                    subprogram += "  " + source + " = " + sourceLocation + "\n";
                } else {
                    source = sourceLocation;
                }
            }

            subprogram += "  " + dest + " = " + source + "\n";

            return subprogram;
        }

        @Override
        public String visit(LinearScan alloc, VCall arg0) throws Exception {
            String subprogram = "";

            int i = 0;
            int out = 0;
            for (VOperand operand : arg0.args) {
                String op = operand.toString();
                if (operand instanceof VVarRef) {
                    op = getStorage(operand.toString(), alloc);
                }

                String reg = "$a" + i;
                if (i > 3) {
                    if (op.startsWith("local[")) {
                        subprogram += "  $v1 = " + op + "\n";
                        op = "$v1";
                    }

                    reg = "out[" + out + ']';
                    out++;
                }
                subprogram += "  " + reg + " = " + op + "\n";
                i++;
            }
            outStackSize = Math.max(outStackSize, out);

            String addr = "";
            if (arg0.addr instanceof VAddr.Label) {
                addr = arg0.addr.toString();
            } else if (arg0.addr instanceof VAddr.Var) {
                final String addrLocation = getStorage(arg0.addr.toString(), alloc);
                addr = addrLocation.startsWith("local") ? "$v0" : addrLocation;
                if (addrLocation.startsWith("local")) {
                    subprogram += "  " + addr + " = " + addrLocation + "\n";
                }
            }

            subprogram += "  call " + addr + "\n";

            final String dest = getStorage(arg0.dest.toString(), alloc);
            if (dest != null) {
                subprogram += "  " + dest + " = $v0\n";
            }

            return subprogram;

        }

        @Override
        public String visit(LinearScan alloc, VBuiltIn arg0) throws Exception {
            String subprogram = "";

            String operandList = "";

            int temp = 0;
            for (VOperand operand : arg0.args) {
                String op = "";
                if (operand instanceof VVarRef.Local) {
                    final String opLocation = getStorage(operand.toString(), alloc);
                    if (opLocation.startsWith("local")) {
                        op = "$v" + temp;
                        subprogram += "  " + op + " = " + opLocation + "\n";
                        temp++;
                    } else {
                        op = opLocation;
                    }
                } else {
                    op = operand.toString();
                }

                operandList += " " + op;
            }
            operandList = "(" + operandList.strip() + ")";

            final String builtinCall = arg0.op.name + operandList;

            String assign = "";
            if (arg0.dest != null) {
                final String destLocation = getStorage(arg0.dest.toString(), alloc);
                if (destLocation != null) {
                    if (destLocation.startsWith("local")) {
                        assign = "$v0 = ";
                    } else if (alloc.aliveAt(arg0.dest.toString(), arg0.sourcePos.line)) {
                        assign = destLocation + " = ";
                    }
                }
            }

            subprogram += "  " + assign + builtinCall + "\n";
            return subprogram;
        }

        @Override
        public String visit(LinearScan alloc, VMemWrite arg0) throws Exception {
            String subprogram = "";

            final VMemRef.Global destination = (VMemRef.Global) arg0.dest;

            String dest = destination.base.toString();
            if (destination.base instanceof VAddr.Var) {
                final String destLocation = getStorage(destination.base.toString(), alloc);
                if (destLocation.startsWith("local")) {
                    dest = "$v0";
                    subprogram += "  " + dest + " = " + destLocation + "\n";
                } else {
                    dest = destLocation;
                }
            }

            String rhs = "";
            if (arg0.source instanceof VVarRef.Local) {
                rhs = alloc.variableRegisters.get(arg0.source.toString());
                final String rhsLocation = getStorage(arg0.source.toString(), alloc);
                if (rhsLocation.startsWith("local")) {
                    rhs = "$v1";
                    subprogram += "  " + rhs + " = " + rhsLocation + "\n";
                } else {
                    rhs = rhsLocation;
                }
            } else if (arg0.source instanceof VLabelRef) {
                rhs = arg0.source.toString();
            } else if (arg0.source instanceof VLitInt) {
                rhs = arg0.source.toString();
            }

            final String offset = destination.byteOffset == 0 ? "" : "+" + destination.byteOffset;
            subprogram += "  [" + dest + offset + "] = " + rhs + "\n";

            return subprogram;
        }

        @Override
        public String visit(LinearScan alloc, VMemRead arg0) throws Exception {
            if (!alloc.aliveAt(arg0.dest.toString(), arg0.sourcePos.line)) {
                return "";
            }

            String subprogram = "";

            final VMemRef.Global source = (VMemRef.Global) arg0.source;

            final String destLocation = getStorage(arg0.dest.toString(), alloc);
            final String srcLocation = getStorage(source.base.toString(), alloc);
            if (destLocation == null || srcLocation == null) {
                return "";
            }

            final String dest = destLocation.startsWith("local") ? "$v0" : destLocation;

            String src = srcLocation;
            if (src.startsWith("local")) {
                if (srcLocation.equals(destLocation)) {
                    src = "$v0";
                } else {
                    src = "$v1";
                }
            }

            if (srcLocation.startsWith("local")) {
                subprogram += "  " + src + " = " + srcLocation + "\n";
            }

            final String offset = source.byteOffset == 0 ? "" : "+" + source.byteOffset;
            subprogram += "  " + dest + " = [" + src + offset + "]\n";

            if (destLocation.startsWith("local")) {
                subprogram += "  " + destLocation + " = " + dest + "\n";
            }

            return subprogram;
        }

        @Override
        public String visit(LinearScan alloc, VBranch arg0) throws Exception {
            String subprogram = "";

            String value = "";
            if (arg0.value instanceof VVarRef.Local) {
                value = getStorage(arg0.value.toString(), alloc);
                if (value.startsWith("local")) {
                    subprogram += "  $v0" + " = " + value + "\n";
                    value = "$v0";
                }
            } else {
                value = arg0.value.toString();
            }

            subprogram += "  if" + (arg0.positive ? "" : 0) + " " + value + " goto " + arg0.target + "\n";

            return subprogram;
        }

        @Override
        public String visit(LinearScan alloc, VGoto arg0) throws Exception {
            return "  goto " + arg0.target.toString() + "\n";
        }

        @Override
        public String visit(LinearScan alloc, VReturn arg0) throws Exception {
            String retVal = arg0.value.toString();
            if (arg0.value instanceof VVarRef) {
                retVal = getStorage(retVal, alloc);
            }

            return "  $v0 = " + retVal + "\n";
        }

        private String getStorage(String variable, LinearScan alloc) {
            String ret = alloc.location.get(variable);
            if (ret == null) {
                ret = alloc.variableRegisters.get(variable);
            }

            return ret;
        }
    }
}
