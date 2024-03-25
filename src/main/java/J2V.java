public class J2V {
    public static void main(String[] args) {
        Typecheck typechecker = new Typecheck();
        if (typechecker.check()) {
            System.out.println("Converting MiniJava to Vapor");
        }
    }
}
