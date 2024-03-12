package picojava;

import java.util.HashMap;

import visitor.GJDepthFirst;

public class SymTableVis<R, A> extends GJDepthFirst<R, A> {
    public HashMap<String, String> symt;

    public SymTableVis() {
        symt = new HashMap<>();
    }
}
