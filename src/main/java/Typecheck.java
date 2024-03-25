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

            TypeCheckSimp check = new TypeCheckSimp();
            final String output = root.accept(check, vis.symt);
            System.out.println(output);
            if (output.equals("error")) {
                System.out.println("Type error");
            } else {
                System.out.println("Program type checked successfully");
            }
        } catch (ParseException e) {
            System.err.println("Error! " + e.getMessage());
        }
    }
}
