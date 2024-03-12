package picojava;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestSymTableVis {
    @Test
    public void testConstruction() {
        SymTableVis<Void, Integer> vis = new SymTableVis<>();
        assertEquals(true, vis.symt.isEmpty());
    }
}
