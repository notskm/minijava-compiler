package minijava;

public class VaporAST {
    public enum Type {
        trivial, nontrivial
    }

    public String tempExprResult = "";
    public String tempExprType = "";
    public Type tempType = null;
    public String subprogram = "";
}
