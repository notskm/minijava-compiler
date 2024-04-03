import static org.junit.jupiter.api.Assertions.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import minijava.MethodTableVis;
import minijava.MiniJavaToVaporVis;
import minijava.SymTableVis;
import minijava.TypeCheckSimp;
import syntaxtree.Node;

public class IntegrationTest {
    @BeforeAll
    public static void setup() {
        new MiniJavaParser(System.in);
    }

    @ParameterizedTest
    @ValueSource(strings = { "Factorial", "BinaryTree", "BubbleSort", "LinearSearch", "LinkedList", "QuickSort",
            "TreeVisitor", "MoreThan4" })
    public void testTypecheckingValidPrograms(String file) {
        final String filename = "programs/java/" + file + ".java";
        try (FileInputStream fStream = new FileInputStream(filename);) {
            MiniJavaParser.ReInit(fStream);

            final Node root = MiniJavaParser.Goal();
            final SymTableVis vis = new SymTableVis();
            final TypeCheckSimp check = new TypeCheckSimp();

            root.accept(vis);

            final String output = root.accept(check, vis.symt);

            assertNotEquals("error", output);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "Factorial", "BinaryTree", "BubbleSort", "LinearSearch", "LinkedList", "QuickSort",
            "TreeVisitor", "TreeVisitor", "MoreThan4" })
    public void testTypecheckingInvalidPrograms(String file) {
        final String filename = "programs/java/" + file + "-error.java";
        try (FileInputStream fStream = new FileInputStream(filename);) {
            MiniJavaParser.ReInit(fStream);

            final Node root = MiniJavaParser.Goal();
            final SymTableVis vis = new SymTableVis();
            final TypeCheckSimp check = new TypeCheckSimp();

            assertThrows(RuntimeException.class, () -> {
                root.accept(vis);
                root.accept(check, vis.symt);
            });
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "Factorial" })
    public void testConversionToVaporValidPrograms(String file) {
        final String filename = "programs/java/" + file + ".java";
        final String vaporFilename = "programs/vapor/" + file + ".vapor";
        try (
                FileInputStream fStream = new FileInputStream(filename);
                FileInputStream vapor = new FileInputStream(vaporFilename);) {
            MiniJavaParser.ReInit(fStream);

            final Node root = MiniJavaParser.Goal();
            final SymTableVis vis = new SymTableVis();
            final TypeCheckSimp check = new TypeCheckSimp();

            root.accept(vis);
            root.accept(check, vis.symt);

            final MethodTableVis methodTableVis = new MethodTableVis();
            root.accept(methodTableVis);

            final MiniJavaToVaporVis m2v = new MiniJavaToVaporVis(methodTableVis.methodTables);
            root.accept(m2v, vis.symt);

            final String output = m2v.toVapor();

            assertEquals(streamToString(vapor), output);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    private String streamToString(InputStream stream) {
        byte[] bytes = new byte[0];

        try {
            bytes = stream.readAllBytes();
        } catch (IOException e) {

        }

        return new String(bytes);
    }
}
