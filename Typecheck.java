import picojava.*;
import syntaxtree.*;

public class Typecheck {
    public static void main(String[] args) {
        try {
            new MiniJavaParser(System.in);
            Node root = MiniJavaParser.Goal();
            PPrinter<Void, String> printer = new PPrinter<>();
            printer.accept(root);
        } catch (ParseException e) {
            System.err.println("Error! " + e.getMessage());
        }
    }
}
