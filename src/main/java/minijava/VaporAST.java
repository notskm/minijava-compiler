package minijava;

public class VaporAST {
    public enum Kind {
        Trivial, Builtin, Call, Deref, None
    }

    public String tempExprResult = "";
    public String tempExprType = "";
    public String subprogram = "";
    public Kind exprType = Kind.None;
}
