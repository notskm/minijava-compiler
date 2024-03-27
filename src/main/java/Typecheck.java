import minijava.*;
import syntaxtree.*;

public class Typecheck {
    public SymbolTable symt;
    public Node root;

    public boolean check() {
        try {
            new MiniJavaParser(System.in);
            root = MiniJavaParser.Goal();
            PPrinter<Void, String> printer = new PPrinter<>();
            printer.accept(root);

            SymTableVis<Void, Integer> vis = new SymTableVis<>();
            root.accept(vis, 0);
            symt = vis.symt;

            TypeCheckSimp check = new TypeCheckSimp();
            final String output = root.accept(check, vis.symt);
            System.out.println(output);
            if (output.equals("error")) {
                return false;
            } else {
                return true;
            }
        } catch (ParseException e) {
            return false;
        }
    };

    public static void main(String[] args) {
        Typecheck typechecker = new Typecheck();
        if (typechecker.check()) {
            System.out.println("Program type checked successfully");
        } else {
            System.out.println("Type error");
        }
    }
}
