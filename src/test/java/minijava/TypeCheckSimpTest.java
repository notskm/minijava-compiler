package minijava;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import syntaxtree.*;

public class TypeCheckSimpTest {
    private TypeCheckSimp check;
    private SymbolTable emptySymbolTable;

    @BeforeEach
    public void setup() {
        check = new TypeCheckSimp();
        emptySymbolTable = new SymbolTable();
    }

    @Test
    public void testTypeOfIntegerLiteralIsInt() {
        final NodeToken token = new NodeToken("1");
        final IntegerLiteral tl = new IntegerLiteral(token);
        final String type = tl.accept(check, emptySymbolTable);
        assertEquals("Int", type);
    }

    @Test
    public void testTypeOfTrueLiteralIsBoolean() {
        final TrueLiteral tl = new TrueLiteral();
        final String type = tl.accept(check, emptySymbolTable);
        assertEquals("Boolean", type);
    }

    @Test
    public void testTypeOfFalseLiteralIsBoolean() {
        final FalseLiteral tl = new FalseLiteral();
        final String type = tl.accept(check, emptySymbolTable);
        assertEquals("Boolean", type);
    }

    @Test
    public void testAllocationExpressionTypeIsNameOfIdentifier() {
        final NodeToken nt = new NodeToken("MyType");
        final Identifier id = new Identifier(nt);
        final AllocationExpression ae = new AllocationExpression(id);

        final String ret = ae.accept(check, emptySymbolTable);

        assertEquals("MyType", ret);
    }

    @Test
    public void testArrayAllocationExpressionIsOfTypeIntArrayWhenGivenAnInt() {
        final NodeToken nt = new NodeToken("1");
        final IntegerLiteral il = new IntegerLiteral(nt);
        final NodeChoice nc = new NodeChoice(il);
        final PrimaryExpression pe = new PrimaryExpression(nc);
        final NodeChoice nc2 = new NodeChoice(pe);
        final Expression e = new Expression(nc2);
        final ArrayAllocationExpression aae = new ArrayAllocationExpression(e);

        final String type = aae.accept(check, emptySymbolTable);

        assertEquals("Int[]", type);
    }

    @Test
    public void testArrayAllocationExpressionProducesErrorWhenNotGivenAnInt() {
        final TrueLiteral tl = new TrueLiteral();
        final NodeChoice nc = new NodeChoice(tl);
        final PrimaryExpression pe = new PrimaryExpression(nc);
        final NodeChoice nc2 = new NodeChoice(pe);
        final Expression e = new Expression(nc2);
        final ArrayAllocationExpression aae = new ArrayAllocationExpression(e);

        final String type = aae.accept(check, emptySymbolTable);

        assertEquals("error", type);
    }

    @Test
    public void testNotExpressionIsOfTypeBooleanWhenExpressionIsOfTypeBoolean() {
        final TrueLiteral tl = new TrueLiteral();
        final NodeChoice nc = new NodeChoice(tl);
        final PrimaryExpression pe = new PrimaryExpression(nc);
        final NodeChoice nc2 = new NodeChoice(pe);
        final Expression e = new Expression(nc2);
        final NotExpression ne = new NotExpression(e);

        final String type = ne.accept(check, emptySymbolTable);

        assertEquals("Boolean", type);
    }

    @Test
    public void testNotExpressionProducesErrorWhenExpressionIsNotBoolean() {
        final NodeToken nt = new NodeToken("1");
        final IntegerLiteral il = new IntegerLiteral(nt);
        final NodeChoice nc = new NodeChoice(il);
        final PrimaryExpression pe = new PrimaryExpression(nc);
        final NodeChoice nc2 = new NodeChoice(pe);
        final Expression e = new Expression(nc2);
        final NotExpression ne = new NotExpression(e);

        final String type = ne.accept(check, emptySymbolTable);

        assertEquals("error", type);
    }

    @Test
    public void testBracketExpressionIsSameTypeAsExpression() {
        final NodeToken token = new NodeToken("1");
        final IntegerLiteral il = new IntegerLiteral(token);
        final NodeChoice nc = new NodeChoice(il);
        final PrimaryExpression pe = new PrimaryExpression(nc);
        final NodeChoice nc2 = new NodeChoice(pe);
        final Expression e = new Expression(nc2);
        final BracketExpression be = new BracketExpression(e);

        final String bracketType = be.accept(check, emptySymbolTable);
        final String expressionType = e.accept(check, emptySymbolTable);

        assertEquals(expressionType, bracketType);
    }
}
