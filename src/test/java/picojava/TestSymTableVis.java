package picojava;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import syntaxtree.*;

public class TestSymTableVis {
    SymTableVis<Void, Integer> vis;

    @BeforeEach
    public void setup() {
        vis = new SymTableVis<>();
    }

    @Test
    public void testConstruction() {
        assertEquals(true, vis.symt.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2, 3 })
    @DisplayName("visiting VarDeclaration adds variable to symbol table")
    public void testVarDeclarationAddsVar(int typeIndex) {
        final Identifier id = new Identifier(new NodeToken("my_var"));
        final Type type = makeType(typeIndex);
        final VarDeclaration decl = new VarDeclaration(type, id);

        decl.accept(vis, 0);

        final String typeString = indexToType(typeIndex);
        assertEquals(typeString, vis.symt.get("my_var"));
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2, 3 })
    @DisplayName("visiting VarDeclaration includes prefix in the symbol table")
    public void testVarDeclarationIncludesPrefix(int typeIndex) {
        final Type type = makeType(typeIndex);
        final Identifier id = new Identifier(new NodeToken("abc"));
        final VarDeclaration decl = new VarDeclaration(type, id);

        vis.setPrefix("test.");

        decl.accept(vis, 0);

        final String typeString = indexToType(typeIndex);
        assertEquals(typeString, vis.symt.get("test.abc"));
    }

    private Type makeType(int typeIndex) {
        String typeString = indexToType(typeIndex);
        if (typeIndex != 3) {
            typeString = typeString.toLowerCase();
        }

        NodeChoice choice;

        if (typeIndex == 3) {
            final Identifier typeName = new Identifier(new NodeToken(typeString));
            choice = new NodeChoice(typeName, typeIndex);
        } else {
            final NodeToken typeToken = new NodeToken(typeString);
            choice = new NodeChoice(typeToken, typeIndex);
        }

        return new Type(choice);
    }

    private String indexToType(int i) {
        switch (i) {
            case 0:
                return "Int[]";
            case 1:
                return "Boolean";
            case 2:
                return "Int";
            case 3:
                return "MyType";
            default:
                return "";
        }
    }
}
