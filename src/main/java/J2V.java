import minijava.MethodTableVis;
import minijava.MiniJavaToVaporVis;
import minijava.SymbolTable;
import minijava.VaporAST;
import syntaxtree.Node;

public class J2V {
    public static String compileToVapor(Node root, SymbolTable symt) {
        MethodTableVis methodTableVis = new MethodTableVis();
        root.accept(methodTableVis);

        MiniJavaToVaporVis vaporVis = new MiniJavaToVaporVis(methodTableVis.methodTables);
        VaporAST vapor = root.accept(vaporVis, symt);

        return vapor.subprogram;
    }

    public static void main(String[] args) {
        Typecheck typechecker = new Typecheck();

        if (typechecker.check()) {
            final Node root = typechecker.root;
            SymbolTable symt = typechecker.symt;

            final String program = compileToVapor(root, symt);
            System.out.println(program);
        }
    }
}
