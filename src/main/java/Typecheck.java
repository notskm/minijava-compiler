import minijava.*;
import syntaxtree.*;

public class Typecheck {
    public SymbolTable symt;
    public Node root;

    private void parse() throws ParseException {
        new MiniJavaParser(System.in);
        root = MiniJavaParser.Goal();
    }

    private void printAST() {
        PPrinter printer = new PPrinter();
        root.accept(printer);
    }

    private boolean typecheck() {
        SymTableVis<Void, Integer> vis = new SymTableVis<>();
        root.accept(vis, 0);
        symt = vis.symt;

        TypeCheckSimp check = new TypeCheckSimp();
        final String output = root.accept(check, vis.symt);

        return !output.equals("error");
    }

    public boolean check() {
        try {
            parse();
            return typecheck();
        } catch (ParseException e) {
            return false;
        }
    };

    public static void main(String[] args) {
        Typecheck typechecker = new Typecheck();
        final boolean checkResult = typechecker.check();

        if (args.length > 0 && args[0].equals("-v")) {
            typechecker.printAST();
        }

        if (checkResult) {
            System.out.println("Program type checked successfully");
        } else {
            System.out.println("Type error");
        }
    }
}
