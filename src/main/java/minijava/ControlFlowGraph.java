package minijava;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cs132.vapor.ast.*;

public class ControlFlowGraph {
    private Node[] allNodes;
    private VFunction function;
    private List<Integer> functionCallLines;

    public ControlFlowGraph(VFunction function) {
        this.function = function;

        Vis vis = new Vis(function);

        for (VInstr instruction : function.body) {
            try {
                instruction.accept(vis);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        vis.computeLiveness();
        allNodes = vis.allNodes.values().toArray(new Node[vis.allNodes.size()]);
        functionCallLines = vis.functionCallLines;
    }

    public LiveInterval[] getLiveIntervals() {
        Map<String, LiveInterval> intervals = new HashMap<>();
        for (Node node : allNodes) {
            final int line = node.instruction.sourcePos.line;
            for (String var : node.liveIn) {
                intervals.putIfAbsent(var, new LiveInterval(var));

                final LiveInterval interval = intervals.get(var);
                interval.startLine = Math.min(interval.startLine, line);
                interval.endLine = Math.max(interval.endLine, line);
            }

            for (String var : node.liveOut) {
                intervals.putIfAbsent(var, new LiveInterval(var));

                final LiveInterval interval = intervals.get(var);
                interval.startLine = Math.min(interval.startLine, line);
                interval.endLine = Math.max(interval.endLine, line);
            }
        }

        for (VVarRef.Local param : function.params) {
            final LiveInterval interval = intervals.get(param.ident);

            // Params that are not used in the function do not get intervals
            if (interval != null) {
                interval.startLine = param.sourcePos.line;
            }
        }

        for (LiveInterval interval : intervals.values()) {
            for (int line : functionCallLines) {
                if (interval.startLine < line && interval.endLine > line) {
                    interval.crossCall = true;
                }
            }
        }

        return intervals.values().toArray(new LiveInterval[intervals.size()]);
    }

    static class LiveInterval {
        public LiveInterval(String var) {
            variable = var;
        }

        public int startLine = Integer.MAX_VALUE;
        public int endLine = Integer.MIN_VALUE;
        public String variable;
        public boolean crossCall = false;

        static class SortByStartIncreasing implements Comparator<LiveInterval> {
            @Override
            public int compare(LiveInterval o1, LiveInterval o2) {
                return Integer.compare(o1.startLine, o2.startLine);
            }
        }

        static class SortByEndIncreasing implements Comparator<LiveInterval> {
            @Override
            public int compare(LiveInterval o1, LiveInterval o2) {
                return Integer.compare(o1.endLine, o2.endLine);
            }
        }
    }

    public static class Node {
        public VInstr instruction;
        public List<Node> predecessors = new ArrayList<>();
        public List<Node> successors = new ArrayList<>();

        public Set<String> use = new HashSet<>();
        public Set<String> def = new HashSet<>();
        public Set<String> liveIn = new HashSet<>();
        public Set<String> liveOut = new HashSet<>();
    }

    private static class Vis extends VInstr.Visitor<Throwable> {
        private Map<Integer, Node> allNodes = new HashMap<>();
        private Map<String, Integer> labelLocations = new HashMap<>();
        private Map<Integer, Node> nextNode = new HashMap<>();

        private List<Integer> functionCallLines = new ArrayList<>();

        public void computeLiveness() {
            Set<String> in = null;
            Set<String> out = null;

            boolean done = false;
            while (!done) {
                done = true;
                for (Node node : allNodes.values()) {
                    in = new HashSet<>(node.liveIn);
                    out = new HashSet<>(node.liveOut);

                    Set<String> diff = new HashSet<>(node.liveOut);
                    diff.removeAll(node.def);
                    Set<String> union = new HashSet<>(node.use);
                    union.addAll(diff);
                    node.liveIn = union;

                    node.liveOut = new HashSet<>();
                    for (Node successor : node.successors) {
                        node.liveOut.addAll(successor.liveIn);
                    }

                    if (!in.equals(node.liveIn)) {
                        done = false;
                    }

                    if (!out.equals(node.liveOut)) {
                        done = false;
                    }
                }
            }
        }

        public Vis(VFunction function) {
            int previousLine = 0;
            for (VInstr instruction : function.body) {
                Node node = new Node();
                node.instruction = instruction;
                nextNode.put(previousLine, node);
                allNodes.put(instruction.sourcePos.line, node);
                previousLine = instruction.sourcePos.line;
            }

            for (VCodeLabel label : function.labels) {
                final int line = function.body[label.instrIndex].sourcePos.line;
                labelLocations.put(label.ident, line);
            }
        }

        @Override
        public void visit(VAssign instr) throws Throwable {
            attachImmediateSuccessor(instr);

            final Node node = allNodes.get(instr.sourcePos.line);
            node.def.add(instr.dest.toString());
            if (instr.source instanceof VVarRef.Local) {
                node.use.add(instr.source.toString());
            }
        }

        @Override
        public void visit(VCall instr) throws Throwable {
            attachImmediateSuccessor(instr);

            final Node node = allNodes.get(instr.sourcePos.line);

            if (instr.dest != null) {
                node.def.add(instr.dest.toString());
            }

            if (instr.addr instanceof VAddr.Var<?>) {
                node.use.add(instr.addr.toString());
            }

            for (VOperand arg : instr.args) {
                if (arg instanceof VVarRef) {
                    node.use.add(arg.toString());
                }
            }

            functionCallLines.add(instr.sourcePos.line);
        }

        @Override
        public void visit(VBuiltIn instr) throws Throwable {
            attachImmediateSuccessor(instr);

            final Node node = allNodes.get(instr.sourcePos.line);

            if (instr.dest != null) {
                node.def.add(instr.dest.toString());
            }

            for (VOperand arg : instr.args) {
                if (arg instanceof VVarRef) {
                    node.use.add(arg.toString());
                }
            }
        }

        @Override
        public void visit(VMemWrite instr) throws Throwable {
            attachImmediateSuccessor(instr);

            final Node node = allNodes.get(instr.sourcePos.line);

            final VMemRef.Global global = (VMemRef.Global) instr.dest;
            if (global.base instanceof VAddr.Var<?>) {
                node.def.add(global.base.toString());
                node.use.add(global.base.toString());
            }

            if (instr.source instanceof VVarRef) {
                node.use.add(instr.source.toString());
            }
        }

        @Override
        public void visit(VMemRead instr) throws Throwable {
            attachImmediateSuccessor(instr);

            final Node node = allNodes.get(instr.sourcePos.line);

            node.def.add(instr.dest.toString());

            final VMemRef.Global global = (VMemRef.Global) instr.source;
            if (global.base instanceof VAddr.Var<?>) {
                node.use.add(global.base.toString());
            }
        }

        @Override
        public void visit(VBranch instr) throws Throwable {
            attachImmediateSuccessor(instr);
            attachLabelSuccessor(instr, instr.target.toString().substring(1));

            final Node node = allNodes.get(instr.sourcePos.line);
            if (instr.value instanceof VVarRef) {
                node.use.add(instr.value.toString());
            }
        }

        @Override
        public void visit(VGoto instr) throws Throwable {
            attachLabelSuccessor(instr, instr.target.toString().substring(1));
        }

        @Override
        public void visit(VReturn instr) throws Throwable {
            attachImmediateSuccessor(instr);

            final Node node = allNodes.get(instr.sourcePos.line);
            if (instr.value instanceof VVarRef) {
                node.use.add(instr.value.toString());
            }
        }

        private void attachLabelSuccessor(VInstr instr, String label) {
            final int labelLine = labelLocations.get(label);
            final Node node = allNodes.get(instr.sourcePos.line);
            final Node successor = allNodes.get(labelLine);
            attach(node, successor);
        }

        private void attachImmediateSuccessor(VInstr instr) {
            final Node node = allNodes.get(instr.sourcePos.line);
            final int line = node.instruction.sourcePos.line;
            final Node successor = nextNode.get(line);
            attach(node, successor);
        }

        private void attach(Node node, Node successor) {
            if (successor != null) {
                node.successors.add(successor);
                successor.predecessors.add(node);
            }
        }
    }
}
