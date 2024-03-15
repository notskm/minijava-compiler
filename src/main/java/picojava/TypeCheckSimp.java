package picojava;

import java.util.HashMap;

import syntaxtree.*;
import visitor.GJDepthFirst;

public class TypeCheckSimp extends GJDepthFirst<String, HashMap<String, String>> {

    public String visit(NodeChoice n, HashMap<String, String> argu) {
        return n.choice.accept(this, argu);
    }

    public String visit(Expression n, HashMap<String, String> argu) {
        return n.f0.accept(this, argu);
    }

    public String visit(PrimaryExpression n, HashMap<String, String> argu) {
        return n.f0.accept(this, argu);
    }

    public String visit(IntegerLiteral n, HashMap<String, String> argu) {
        return "Int";
    }

    public String visit(TrueLiteral n, HashMap<String, String> argu) {
        return "Boolean";
    }

    public String visit(FalseLiteral n, HashMap<String, String> argu) {
        return "Boolean";
    }

    public String visit(ArrayAllocationExpression n, HashMap<String, String> argu) {
        final String type = n.f3.accept(this, argu);

        if (type.equals("Int")) {
            return "Int[]";
        } else {
            return "error";
        }
    }

    public String visit(AllocationExpression n, HashMap<String, String> argu) {
        return n.f1.f0.toString();
    }

    public String visit(NotExpression n, HashMap<String, String> argu) {
        final String type = n.f1.accept(this, argu);
        if (type.equals("Boolean")) {
            return type;
        } else {
            return "error";
        }
    }

    public String visit(BracketExpression n, HashMap<String, String> argu) {
        return n.f1.accept(this, argu);
    }
}
