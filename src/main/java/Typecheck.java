import minijava.*;
import syntaxtree.*;

public class Typecheck {
    public SymbolTable symt = new SymbolTable();
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
        SymTableVis vis = new SymTableVis();
        TypeCheckSimp check = new TypeCheckSimp();

        try {
            root.accept(vis);
            root.accept(check, vis.symt);
            symt = vis.symt;
            return true;
        } catch (TypecheckException e) {
            return false;
        }
    }

    public boolean check() {
        try {
            parse();
            return typecheck();
        } catch (ParseException e) {
            System.err.println(e.getMessage());
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
