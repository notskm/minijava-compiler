import static org.junit.jupiter.api.Assertions.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import minijava.SymTableVis;
import minijava.TypeCheckSimp;
import syntaxtree.Node;

public class IntegrationTest {
    @BeforeAll
    public static void setup() {
        new MiniJavaParser(System.in);
    }

    @ParameterizedTest
    @ValueSource(strings = { "Factorial", "BinaryTree", "BubbleSort",
            "LinearSearch", "LinkedList", "QuickSort",
            "TreeVisitor", "MoreThan4", "OverrideVariable", "OverrideMethod", "test66" })
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
            fail(e);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "Factorial", "BinaryTree", "BubbleSort",
            "LinearSearch", "LinkedList", "QuickSort",
            "TreeVisitor", "TreeVisitor", "MoreThan4", "Overloading",
            "OverloadingInheritance", "OverloadingIndirect",
            "DistinctMain", "DistinctClasses", "RealTypes", "OverrideParameter",
            "DistinctMethods", "NonexistentBase" })
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
    @ValueSource(strings = { "Factorial", "MoreThan4", "BubbleSort",
            "LinearSearch", "QuickSort",
            "BinaryTree", "LinkedList", "TreeVisitor", "test03Add", "test03Mult", "test07", "ArrayLookup",
            "Inheritance" })
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

            final String output = J2V.compileToVapor(root, vis.symt);

            assertEquals(streamToString(vapor), output);
        } catch (Exception e) {
            fail(e);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "Factorial", "MoreThan4", "While", "BubbleSort",
            "LinearSearch", "QuickSort",
            "BinaryTree", "LinkedList", "TreeVisitor", "test03Add", "test03Mult", "test07", "ArrayLookup" })
    public void testVaporBehavior(String file) {
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

            final String output = J2V.compileToVapor(root, vis.symt);
            Files.write(Paths.get("testvapor.out"), output.getBytes());

            final String expected = runVaporProgram(vaporFilename);
            final String result = runVaporProgram("testvapor.out");

            assertEquals(expected, result);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    private String runVaporProgram(String filename) throws IOException,
            InterruptedException {
        Runtime rt = Runtime.getRuntime();
        String[] commands = { "java", "-jar", "tools/vapor.jar", "run", filename };
        Process proc = rt.exec(commands, null);
        byte[] bytes = proc.getInputStream().readAllBytes();
        proc.waitFor();
        proc.getInputStream().close();
        proc.destroyForcibly();
        return new String(bytes);
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
