package minijava;

import java.util.Map;

import cs132.vapor.ast.*;
import cs132.vapor.ast.VMemRef.Global;

public class InstructionVis extends VInstr.VisitorPR<SpillEverywhere, String, Exception> {
    @Override
    public String visit(SpillEverywhere alloc, VAssign arg0) throws Exception {
        String subprogram = "";

        String rhs = alloc.localMap.get(arg0.source.toString());
        if (rhs == null) {
            rhs = arg0.source.toString();
        } else {
            subprogram += "  $t2 = " + rhs + "\n";
            rhs = "$t2";
        }

        subprogram += "  $t0 = " + rhs + "\n";
        subprogram += "  " + alloc.localMap.get(arg0.dest.toString()) + " = $t0\n";

        return subprogram;
    }

    @Override
    public String visit(SpillEverywhere alloc, VCall arg0) throws Exception {
        String subprogram = "";

        int i = 0;
        int out = 0;
        for (VOperand operand : arg0.args) {
            String op = alloc.localMap.get(operand.toString());
            if (op == null) {
                op = operand.toString();
            }

            String reg = "$a" + i;
            if (i >= 3) {
                reg = "out[" + out + ']';
                out++;
            }
            subprogram += "  $t1 = " + op + "\n";
            subprogram += "  " + reg + " = $t1\n";
            op = reg;
            i++;
        }

        String addr = alloc.localMap.get(arg0.addr.toString());
        if (addr == null) {
            addr = arg0.addr.toString();
        }
        subprogram += "  $t1 = " + addr + "\n";
        subprogram += "  call $t1\n";

        if (arg0.dest != null) {
            subprogram += "  " + alloc.localMap.get(arg0.dest.toString()) + " = $v0\n";
        }

        return subprogram;

    }

    @Override
    public String visit(SpillEverywhere alloc, VBuiltIn arg0) throws Exception {
        String subprogram = "";

        String operandList = "";
        int i = 1;
        for (VOperand operand : arg0.args) {
            String op = alloc.localMap.get(operand.toString());
            if (op == null) {
                op = operand.toString();
            } else {
                final String reg = "$t" + i;
                subprogram += "  " + reg + " = " + op + "\n";
                op = reg;
                i++;
            }

            operandList += " " + op;
        }
        operandList = "(" + operandList.strip() + ")";

        final String builtinCall = arg0.op.name + operandList;

        if (arg0.dest != null) {
            subprogram += "  $t0 = " + builtinCall + "\n";
            subprogram += "  " + alloc.localMap.get(arg0.dest.toString()) + " = $t0\n";
        } else {
            subprogram += "  " + builtinCall + "\n";
        }

        return subprogram;
    }

    @Override
    public String visit(SpillEverywhere alloc, VMemWrite arg0) throws Exception {
        String subprogram = "";

        final Global dest = (Global) arg0.dest;

        subprogram += "  $t0 = " + alloc.localMap.get(dest.base.toString()) + "\n";

        String rhs = alloc.localMap.get(arg0.source.toString());
        if (rhs == null) {
            rhs = arg0.source.toString();
        } else {
            subprogram += "  $t1 = " + rhs + "\n";
            rhs = "$t1";
        }

        subprogram += "  [$t0+" + dest.byteOffset + "] = " + rhs + "\n";
        subprogram += "  " + alloc.localMap.get(dest.base.toString()) + " = $t0\n";

        return subprogram;
    }

    @Override
    public String visit(SpillEverywhere alloc, VMemRead arg0) throws Exception {
        String subprogram = "";

        final Global source = (Global) arg0.source;

        subprogram += "  $t1 = " + alloc.localMap.get(source.base.toString()) + "\n";
        subprogram += "  $t0 = [$t1" + "+" + source.byteOffset + "]\n";
        subprogram += "  " + alloc.localMap.get(arg0.dest.toString()) + " = $t0\n";

        return subprogram;
    }

    @Override
    public String visit(SpillEverywhere alloc, VBranch arg0) throws Exception {
        String subprogram = "";

        String value = alloc.localMap.get(arg0.value.toString());
        if (value == null) {
            value = arg0.value.toString();
        }

        subprogram += "  $t1 = " + value + "\n";
        subprogram += "  if" + (arg0.positive ? "" : 0) + " $t1" + " goto " + arg0.target + "\n";

        return subprogram;
    }

    @Override
    public String visit(SpillEverywhere alloc, VGoto arg0) throws Exception {
        return "  goto " + arg0.target.toString() + "\n";
    }

    @Override
    public String visit(SpillEverywhere alloc, VReturn arg0) throws Exception {
        String subprogram = "";
        if (arg0.value != null) {
            String ret = alloc.localMap.get(arg0.value.toString());
            if (ret == null) {
                ret = arg0.value.toString();
            }
            subprogram += "  $v0 = " + ret + "\n";
        }

        for (Map.Entry<String, String> pair : alloc.calleeMap.entrySet()) {
            final String saveReg = pair.getValue();
            subprogram += "  " + saveReg + " = " + alloc.localMap.get(saveReg) + "\n";
        }

        subprogram += "  ret\n";
        return subprogram;
    }
}
