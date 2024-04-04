import cs132.util.ProblemException;
import cs132.vapor.ast.VBuiltIn.Op;
import cs132.vapor.ast.VFunction;
import cs132.vapor.ast.VaporProgram;
import cs132.vapor.parser.VaporParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class V2VM {
    public static void main(String[] args) {
        Typecheck typechecker = new Typecheck();

        if (typechecker.check()) {
            final String program = J2V.compileToVapor(typechecker.root, typechecker.symt);
            final ByteArrayInputStream reader = new ByteArrayInputStream(program.getBytes());

            try {
                final VaporProgram vaporProgram = parseVapor(reader, System.err);
                for (VFunction f : vaporProgram.functions) {
                    System.out.println(f.ident);
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    public static VaporProgram parseVapor(InputStream in, PrintStream err)
            throws IOException {
        Op[] ops = {
                Op.Add, Op.Sub, Op.MulS, Op.Eq, Op.Lt, Op.LtS,
                Op.PrintIntS, Op.HeapAllocZ, Op.Error,
        };

        boolean allowLocals = true;
        String[] registers = null;
        boolean allowStack = false;

        VaporProgram program;
        try {
            program = VaporParser.run(new InputStreamReader(in), 1, 1,
                    java.util.Arrays.asList(ops),
                    allowLocals, registers, allowStack);
        } catch (ProblemException ex) {
            err.println(ex.getMessage());
            return null;
        }

        return program;
    }
}
