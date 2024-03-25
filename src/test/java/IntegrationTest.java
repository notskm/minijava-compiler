import static org.junit.jupiter.api.Assertions.*;

import java.io.FileInputStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import picojava.SymTableVis;
import picojava.TypeCheckSimp;
import syntaxtree.Node;

public class IntegrationTest {
    @BeforeAll
    public static void setup() {
        new MiniJavaParser(System.in);
    }

    @ParameterizedTest
    @ValueSource(strings = { "Factorial", "BinaryTree", "BubbleSort", "LinearSearch", "LinkedList", "QuickSort",
            "TreeVisitor", "MoreThan4" })
    public void asdjfgf(String file) {
        final String filename = "programs/java/" + file + ".java";
        try (FileInputStream fStream = new FileInputStream(filename);) {
            MiniJavaParser.ReInit(fStream);

            final Node root = MiniJavaParser.Goal();
            final SymTableVis<Void, Integer> vis = new SymTableVis<>();
            final TypeCheckSimp check = new TypeCheckSimp();

            root.accept(vis, 0);

            final String output = root.accept(check, vis.symt);

            assertNotEquals("error", output);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "Factorial", "BinaryTree", "BubbleSort", "LinearSearch", "LinkedList", "QuickSort",
            "TreeVisitor", "TreeVisitor", "MoreThan4" })
    public void asdjfgf2(String file) {
        final String filename = "programs/java/" + file + "-error.java";
        try (FileInputStream fStream = new FileInputStream(filename);) {
            MiniJavaParser.ReInit(fStream);

            final Node root = MiniJavaParser.Goal();
            final SymTableVis<Void, Integer> vis = new SymTableVis<>();
            final TypeCheckSimp check = new TypeCheckSimp();

            root.accept(vis, 0);

            final String output = root.accept(check, vis.symt);

            assertEquals("error", output);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
