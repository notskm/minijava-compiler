import minijava.MethodTableVis;
import minijava.MiniJavaToVaporVis;
import minijava.SymbolTable;
import syntaxtree.Node;

public class J2V {
    public static void main(String[] args) {
        Typecheck typechecker = new Typecheck();

        if (typechecker.check()) {
            Node root = typechecker.root;
            SymbolTable symt = typechecker.symt;

            MethodTableVis methodTableVis = new MethodTableVis();
            root.accept(methodTableVis);

            MiniJavaToVaporVis vaporVis = new MiniJavaToVaporVis(methodTableVis.methodTables);
            root.accept(vaporVis, symt);

            System.out.println(vaporVis.toVapor());
        }
    }
}
