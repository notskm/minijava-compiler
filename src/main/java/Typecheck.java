import java.util.HashMap;

import picojava.*;
import syntaxtree.*;

public class Typecheck {
    public static void main(String[] args) {
        try {
            new MiniJavaParser(System.in);
            Node root = MiniJavaParser.Goal();
            PPrinter<Void, String> printer = new PPrinter<>();
            printer.accept(root);

            SymTableVis<Void, Integer> vis = new SymTableVis<>();
            root.accept(vis, 0);
            HashMap<String, String> symt = vis.symt;

            System.out.println();
            System.out.println("Symbol table:");
            for (HashMap.Entry<String, String> entry : symt.entrySet()) {
                System.out.printf("%s: %s%n", entry.getKey(), entry.getValue());
            }
        } catch (ParseException e) {
            System.err.println("Error! " + e.getMessage());
        }
    }
}
