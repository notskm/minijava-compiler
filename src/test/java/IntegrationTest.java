import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

    @ParameterizedTest
    @ValueSource(strings = { "Factorial", "MoreThan4", "BubbleSort", "LinearSearch", "QuickSort", "BinaryTree",
            "LinkedList", "TreeVisitor", "Spill" })
    public void testVaporMBehavior(String file) {
        final String vaporFilename = "programs/vapor/" + file + ".vapor";
        final String vaporMFilename = "programs/vaporm/" + file + ".vaporm";
        try (
                FileInputStream vapor = new FileInputStream(vaporFilename);
                FileInputStream vaporm = new FileInputStream(vaporMFilename);) {

            final String output = V2VM.compile(vapor, System.err);
            Files.write(Paths.get("testvaporm.out"), output.getBytes());

            final String expected = runVaporMProgram(vaporMFilename);
            final String result = runVaporMProgram("testvaporm.out");

            assertEquals(expected, result);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "And", "ArrayLookup", "BigTest", "BinaryTree", "BubbleSort", "Factorial", "Inheritance",
            "LinearSearch", "LinkedList", "MoreThan4", "OverrideMethod", "OverrideVariable", "QuickSort", "test03Add",
            "test03Mult", "test07", "test66", "TreeVisitor", "While" })
    public void testJavaToVaporMBehavior(String file) {
        final String filename = "programs/java/" + file + ".java";
        try (
                FileInputStream fStream = new FileInputStream(filename); //
                FileOutputStream vaporFile = new FileOutputStream("testvaporm.out");//
        ) {
            MiniJavaParser.ReInit(fStream);

            final Node root = MiniJavaParser.Goal();
            final SymTableVis vis = new SymTableVis();
            final TypeCheckSimp check = new TypeCheckSimp();

            root.accept(vis);
            root.accept(check, vis.symt);

            final String vaporProgram = J2V.compileToVapor(root, vis.symt);
            InputStream vaporStream = new ByteArrayInputStream(vaporProgram.getBytes());
            final String vaporMProgram = V2VM.compile(vaporStream, System.err);
            vaporFile.write(vaporMProgram.getBytes());
            vaporFile.close();

            final String javaOutput = runJavaProgram(filename);
            final String vaporMOutput = runVaporMProgram("testvaporm.out");

            assertEquals(javaOutput, vaporMOutput);
        } catch (

        Exception e) {
            fail(e);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "Factorial", "MoreThan4", "BubbleSort",
            "LinearSearch", "QuickSort", "BinaryTree",
            "LinkedList", "TreeVisitor", "InOutLocal", "Spill" })
    public void testVaporMToMipsBehavior(String file) {
        final String filename = "programs/vaporm/" + file + ".vaporm";
        try (
                FileInputStream fStream = new FileInputStream(filename);
                FileOutputStream out = new FileOutputStream("testmips.out");) {

            final String output = VM2M.compile(fStream, System.err);
            out.write(output.getBytes());
            out.close();

            String vaporMOut = (runVaporMProgram(filename) + "\n").replaceAll("\\r\\n?", "\n");
            String mipsOut = runMipsProgram("testmips.out").replaceAll("\\r\\n?", "\n");

            assertEquals(vaporMOut, mipsOut);
        } catch (Exception e) {
            fail(e);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "And", "ArrayLookup", "BigTest", "BinaryTree", "BubbleSort", "Factorial", "Inheritance",
            "LinearSearch", "LinkedList", "MoreThan4", "OverrideMethod", "OverrideVariable", "QuickSort", "test03Add",
            "test03Mult", "test07", "TreeVisitor", "While" })
    public void testFullCompilationToMips(String file) {
        final String filename = "programs/java/" + file + ".java";
        try (
                FileInputStream fStream = new FileInputStream(filename); //
                FileOutputStream mipsFile = new FileOutputStream("testmips.out");//
        ) {
            MiniJavaParser.ReInit(fStream);

            final Node root = MiniJavaParser.Goal();
            final SymTableVis vis = new SymTableVis();
            final TypeCheckSimp check = new TypeCheckSimp();

            root.accept(vis);
            root.accept(check, vis.symt);

            final String vaporProgram = J2V.compileToVapor(root, vis.symt);
            InputStream vaporStream = new ByteArrayInputStream(vaporProgram.getBytes());
            final String vaporMProgram = V2VM.compile(vaporStream, System.err);
            InputStream vaporMStream = new ByteArrayInputStream(vaporMProgram.getBytes());
            final String mipsProgram = VM2M.compile(vaporMStream, System.err);

            mipsFile.write(mipsProgram.getBytes());
            mipsFile.close();

            final String javaOutput = runJavaProgram(filename).replaceAll("\\r\\n?", "\n") + "\n";
            final String mipsOutput = runMipsProgram("testmips.out").replaceAll("\\r\\n?", "\n");

            assertEquals(javaOutput, mipsOutput);
        } catch (

        Exception e) {
            fail(e);
        }
    }

    private String runMipsProgram(String filename) throws IOException, InterruptedException {
        Runtime rt = Runtime.getRuntime();
        String[] commands = { "java", "-jar", "tools/mars.jar", "ascii", "nc", filename };
        Process proc = rt.exec(commands, null);
        byte[] bytes = proc.getInputStream().readAllBytes();
        proc.waitFor();
        proc.getInputStream().close();
        proc.destroyForcibly();
        return new String(bytes);
    }

    private String runJavaProgram(String filename) throws IOException, InterruptedException {
        Runtime rt = Runtime.getRuntime();
        String[] commands = { "java", filename };
        Process proc = rt.exec(commands, null);
        byte[] bytes = proc.getInputStream().readAllBytes();
        proc.waitFor();
        proc.getInputStream().close();
        proc.destroyForcibly();
        return new String(bytes);
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

    private String runVaporMProgram(String filename) throws IOException,
            InterruptedException {
        Runtime rt = Runtime.getRuntime();
        String[] commands = { "java", "-jar", "tools/vapor.jar", "run", "-mips", filename };
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
