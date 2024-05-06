package minijava;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Queue;

import cs132.vapor.ast.VFunction;
import minijava.ControlFlowGraph.LiveInterval;
import minijava.ControlFlowGraph.LiveInterval.SortByEndIncreasing;
import minijava.ControlFlowGraph.LiveInterval.SortByStartIncreasing;

public class LinearScan {
    private static final String[] usableRegisters = { "$t0", "$t1", "$t2", "$t3", "$t4", "$t5", "$t6", "$t7", "$t8" };
    private static final String[] usableCalleeRegisters = { "$s0", "$s1", "$s2", "$s3", "$s4", "$s5", "$s6", "$s7" };

    private List<LiveInterval> intervals;
    private Map<String, LiveInterval> intervalMap;

    private List<String> free = new LinkedList<>();
    private List<LiveInterval> active = new ArrayList<>();
    private Queue<String> freeCallee = new LinkedList<>();

    public Map<String, String> variableRegisters = new HashMap<>();
    public Map<String, String> location = new HashMap<>();
    public Map<String, SpillRestore> spillRestore = new HashMap<>();
    public List<String> calleeRegisters = new ArrayList<>();

    static class SpillRestore {
        public int spillPoint;
        public int restorePoint;
    }

    private int stackLocation = 0;

    public LinearScan(VFunction function) {
        intervalMap = new ControlFlowGraph(function).getLiveIntervals();
        intervals = new ArrayList<LiveInterval>(intervalMap.values());
        intervals.sort(new SortByStartIncreasing());

        free.addAll(Arrays.asList(usableRegisters));
        freeCallee.addAll(Arrays.asList(usableCalleeRegisters));

        calleeRegisterAllocation();
        linearScanRegisterAllocation();
        return;
    }

    public boolean aliveAt(String variable, int line) {
        LiveInterval interval = intervalMap.get(variable);
        if (interval == null) {
            return false;
        } else {
            return interval.startLine <= line && interval.endLine > line;
        }
    }

    private void calleeRegisterAllocation() {
        ListIterator<LiveInterval> iter = intervals.listIterator();

        while (iter.hasNext()) {
            LiveInterval i = iter.next();

            if (i.crossCall) {
                final String reg = freeCallee.poll();
                if (reg == null) {
                    // variableRegisters.put(i.variable, newStackLocation());
                    location.put(i.variable, newStackLocation());
                } else {
                    calleeRegisters.add(reg);
                    variableRegisters.put(i.variable, reg);
                    stackLocation++;
                }

                iter.remove();
            }
        }
    }

    private void linearScanRegisterAllocation() {
        active = new ArrayList<>();

        for (LiveInterval i : intervals) {
            expireOldInterval(i);
            if (active.size() == usableRegisters.length) {
                spillAtInterval(i);
            } else {
                final String register = free.remove(0);
                variableRegisters.put(i.variable, register);
                active.add(i);
                Collections.sort(active, new SortByEndIncreasing());
            }
        }
    }

    private void expireOldInterval(LiveInterval i) {
        ListIterator<LiveInterval> iter = active.listIterator();
        while (iter.hasNext()) {
            LiveInterval j = iter.next();

            if (j.endLine > i.startLine) {
                return;
            }

            iter.remove();
            final String reg = variableRegisters.get(j.variable);
            free.add(0, reg);
        }
    }

    private void spillAtInterval(LiveInterval i) {
        final LiveInterval spill = active.get(active.size() - 1);
        if (spill.endLine > i.endLine) {
            variableRegisters.put(i.variable, variableRegisters.get(spill.variable));
            location.put(spill.variable, newStackLocation());
            active.remove(active.size() - 1);
            active.add(i);
            Collections.sort(active, new SortByEndIncreasing());

            setSpillPoint(spill.variable, i.startLine);
            setRestorePoint(spill.variable, i.endLine);
        } else {
            location.put(i.variable, newStackLocation());

            // setSpillPoint(i.variable, i.startLine);
            // setRestorePoint(i.variable, Integer.MAX_VALUE);
        }
    }

    private String newStackLocation() {
        final String local = "local[" + stackLocation + "]";
        stackLocation++;
        return local;
    }

    private void setSpillPoint(String variable, int line) {
        spillRestore.putIfAbsent(variable, new SpillRestore());
        SpillRestore spill = spillRestore.get(variable);
        spill.spillPoint = line;
    }

    private void setRestorePoint(String variable, int line) {
        spillRestore.putIfAbsent(variable, new SpillRestore());
        SpillRestore spill = spillRestore.get(variable);
        spill.restorePoint = line;
    }
}
