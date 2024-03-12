package picojava;

import java.util.HashMap;

import syntaxtree.*;
import visitor.GJDepthFirst;

public class SymTableVis<R, A> extends GJDepthFirst<R, A> {
    public HashMap<String, String> symt;
    private String keyPrefix = "";

    public SymTableVis() {
        symt = new HashMap<>();
    }

    @Override
    public R visit(MainClass n, A argu) {
        final String prefix = keyPrefix;
        keyPrefix += n.f1.f0.toString() + ".";

        final String main = n.f6.toString();
        final String args = n.f11.f0.toString();

        symt.put(keyPrefix + main, n.f5.toString());
        symt.put(keyPrefix + main + "." + args, n.f8.toString());

        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);
        n.f6.accept(this, argu);
        n.f7.accept(this, argu);
        n.f8.accept(this, argu);
        n.f9.accept(this, argu);
        n.f10.accept(this, argu);
        n.f11.accept(this, argu);
        n.f12.accept(this, argu);
        n.f13.accept(this, argu);
        n.f14.accept(this, argu);
        n.f15.accept(this, argu);
        n.f16.accept(this, argu);
        n.f17.accept(this, argu);

        keyPrefix = prefix;
        return null;
    }

    @Override
    public R visit(ClassDeclaration n, A argu) {
        final String prefix = keyPrefix;
        keyPrefix += n.f1.f0.toString() + ".";

        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);

        keyPrefix = prefix;
        return null;
    }

    @Override
    public R visit(ClassExtendsDeclaration n, A argu) {
        String prefix = keyPrefix;
        keyPrefix += n.f1.f0.toString() + ".";

        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);
        n.f6.accept(this, argu);
        n.f7.accept(this, argu);

        keyPrefix = prefix;
        return null;
    }

    @Override
    public R visit(MethodDeclaration n, A argu) {
        final String methodName = n.f2.f0.toString();
        final String key = keyPrefix + methodName;
        // FIXME: Error if key already exists
        symt.put(key, getTypeAsString(n.f1));

        final String prefix = keyPrefix;
        keyPrefix += methodName + ".";

        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);
        n.f6.accept(this, argu);
        n.f7.accept(this, argu);
        n.f8.accept(this, argu);
        n.f9.accept(this, argu);
        n.f10.accept(this, argu);
        n.f11.accept(this, argu);
        n.f12.accept(this, argu);

        keyPrefix = prefix;

        return null;
    }

    @Override
    public R visit(FormalParameter n, A argu) {
        final String type = getTypeAsString(n.f0);
        final String id = n.f1.f0.toString();

        symt.put(keyPrefix + id, type);

        return null;
    }

    public R visit(VarDeclaration n, A argu) {
        String type = getTypeAsString(n.f0);

        String id = n.f1.f0.tokenImage;

        // FIXME: Error if key already exists
        symt.put(keyPrefix + id, type);

        return null;
    }

    public R visit(Block n, A argu) {
        final String prefix = keyPrefix;
        keyPrefix += argu + ".";

        // TODO: Decide how to number blocks if necessary
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);

        keyPrefix = prefix;

        return null;
    }

    private String getTypeAsString(Type n) {
        switch (n.f0.which) {
            case 0:
                return "Int[]";
            case 1:
                return "Boolean";
            case 2:
                return "Int";
            case 3:
                return ((Identifier) n.f0.choice).f0.toString();
            default:
                // FIXME: Should we produce an error?
                return "";
        }
    }
}
