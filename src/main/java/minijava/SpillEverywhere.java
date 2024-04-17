package minijava;

import java.util.Map;
import java.util.HashMap;

import cs132.vapor.ast.*;

public class SpillEverywhere {
    public final Map<String, String> localMap;
    public final Map<String, String> calleeMap;
    public final int stackSize;
    public final int inStackSize;
    public final int outStackSize;

    public SpillEverywhere(VFunction function) {
        SpillEverywhereVis vis = new SpillEverywhereVis();

        Map<String, String> callees = new HashMap<>();
        int stack = 0;
        for (int i = 0, j = 0; i < function.params.length; i++) {
            final String param = function.params[i].toString();

            if (i < 3) {
                vis.idMap.putIfAbsent(param, "$s" + i);
                vis.idMap.putIfAbsent("$s" + i, "local[" + i + "]");
                callees.putIfAbsent("$a" + i, "$s" + i);
                stack++;
            } else {
                vis.idMap.putIfAbsent(param, "in[" + j + "]");
                j++;
            }
        }
        calleeMap = callees;

        int regNum = function.params.length;
        for (final String var : function.vars) {
            final String reg = "local[" + regNum + "]";
            if (vis.idMap.putIfAbsent(var, reg) == null) {
                regNum++;
            }
            stack++;
        }

        for (VInstr instruction : function.body) {
            try {
                instruction.accept(vis);
            } catch (Throwable e) {

            }
        }

        inStackSize = Math.max(0, function.params.length - 3);
        outStackSize = vis.outStackSize;
        localMap = vis.idMap;
        stackSize = stack;
    }

    private static class SpillEverywhereVis extends VInstr.Visitor<Throwable> {
        public Map<String, String> idMap = new HashMap<>();
        int outStackSize = 0;

        @Override
        public void visit(VAssign arg0) throws Throwable {
        }

        @Override
        public void visit(VCall arg0) throws Throwable {
            outStackSize = Math.max(outStackSize, arg0.args.length - 3);
        }

        @Override
        public void visit(VBuiltIn arg0) throws Throwable {
        }

        @Override
        public void visit(VMemWrite arg0) throws Throwable {
        }

        @Override
        public void visit(VMemRead arg0) throws Throwable {
        }

        @Override
        public void visit(VBranch arg0) throws Throwable {
        }

        @Override
        public void visit(VGoto arg0) throws Throwable {
        }

        @Override
        public void visit(VReturn arg0) throws Throwable {
        }

    }
}
