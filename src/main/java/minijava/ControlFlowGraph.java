package minijava;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cs132.vapor.ast.VAssign;
import cs132.vapor.ast.VBranch;
import cs132.vapor.ast.VBuiltIn;
import cs132.vapor.ast.VCall;
import cs132.vapor.ast.VCodeLabel;
import cs132.vapor.ast.VFunction;
import cs132.vapor.ast.VGoto;
import cs132.vapor.ast.VInstr;
import cs132.vapor.ast.VMemRead;
import cs132.vapor.ast.VMemWrite;
import cs132.vapor.ast.VReturn;

public class ControlFlowGraph {
    private Node root = new Node();

    public ControlFlowGraph(VFunction function) {
        Vis vis = new Vis(function);

        for (VInstr instruction : function.body) {
            try {
                instruction.accept(root, vis);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        root = vis.root;
    }

    public static class Node {
        public VInstr instruction;
        public List<Node> predecessors = new ArrayList<>();
        public List<Node> successors = new ArrayList<>();
    }

    private static class Vis extends VInstr.VisitorP<Node, Throwable> {
        public Node root = null;
        private Map<Integer, Node> allNodes = new HashMap<>();
        private Map<String, Integer> labelLocations = new HashMap<>();
        private Map<Integer, Node> nextNode = new HashMap<>();

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

            root = allNodes.get(function.body[0].sourcePos.line);
        }

        @Override
        public void visit(Node arg0, VAssign arg1) throws Throwable {
            attachImmediateSuccessor(arg1);
        }

        @Override
        public void visit(Node arg0, VCall arg1) throws Throwable {
            attachImmediateSuccessor(arg1);
        }

        @Override
        public void visit(Node arg0, VBuiltIn arg1) throws Throwable {
            attachImmediateSuccessor(arg1);
        }

        @Override
        public void visit(Node arg0, VMemWrite arg1) throws Throwable {
            attachImmediateSuccessor(arg1);
        }

        @Override
        public void visit(Node arg0, VMemRead arg1) throws Throwable {
            attachImmediateSuccessor(arg1);
        }

        @Override
        public void visit(Node arg0, VBranch arg1) throws Throwable {
            attachImmediateSuccessor(arg1);
            attachLabelSuccessor(arg1, arg1.target.toString().substring(1));
        }

        @Override
        public void visit(Node arg0, VGoto arg1) throws Throwable {
            attachLabelSuccessor(arg1, arg1.target.toString().substring(1));
        }

        @Override
        public void visit(Node arg0, VReturn arg1) throws Throwable {
            attachImmediateSuccessor(arg1);
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
