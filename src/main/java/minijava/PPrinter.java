package minijava;

import syntaxtree.*;
import visitor.GJDepthFirst;

public class PPrinter<R, A> extends GJDepthFirst<R, A> {
    private int indent = 0;

    public void accept(Node node) {
        node.accept(this, null);
    }

    public R visit(NodeToken n, A argu) {
        printIndent();
        System.out.print(n.getClass().getSimpleName());
        System.out.print(" => ");
        System.out.println(n.toString());
        return null;
    }

    public R visit(Goal n, A argu) {
        printClassName(n);
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        indent--;
        return null;
    }

    public R visit(MainClass n, A argu) {
        printClassName(n);
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);
        n.f6.accept(this, argu);
        n.f7.accept(this, argu);
        n.f8.accept(this, argu);
        n.f9.accept(this, argu);
        n.f10.accept(this, argu);
        n.f11.accept(this, argu);
        n.f12.accept(this, argu);
        n.f13.accept(this, argu);
        n.f14.accept(this, argu);
        n.f15.accept(this, argu);
        n.f16.accept(this, argu);
        n.f17.accept(this, argu);
        indent--;
        return null;
    }

    public R visit(TypeDeclaration n, A argu) {
        printClassName(n);
        n.f0.accept(this, argu);
        indent--;
        return null;
    }

    public R visit(ClassDeclaration n, A argu) {
        printClassName(n);
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);
        indent--;
        return null;
    }

    public R visit(ClassExtendsDeclaration n, A argu) {
        printClassName(n);
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);
        n.f6.accept(this, argu);
        n.f7.accept(this, argu);
        indent--;
        return null;
    }

    public R visit(VarDeclaration n, A argu) {
        printClassName(n);
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        indent--;
        return null;
    }

    public R visit(MethodDeclaration n, A argu) {
        printClassName(n);
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);
        n.f6.accept(this, argu);
        n.f7.accept(this, argu);
        n.f8.accept(this, argu);
        n.f9.accept(this, argu);
        n.f10.accept(this, argu);
        n.f11.accept(this, argu);
        n.f12.accept(this, argu);
        indent--;
        return null;
    }

    public R visit(FormalParameterList n, A argu) {
        printClassName(n);
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        indent--;
        return null;
    }

    public R visit(FormalParameter n, A argu) {
        printClassName(n);
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        indent--;
        return null;
    }

    public R visit(FormalParameterRest n, A argu) {
        printClassName(n);
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        indent--;
        return null;
    }

    public R visit(Type n, A argu) {
        printClassName(n);
        n.f0.accept(this, argu);
        indent--;
        return null;
    }

    public R visit(ArrayType n, A argu) {
        printClassName(n);
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        indent--;
        return null;
    }

    public R visit(BooleanType n, A argu) {
        printClassName(n);
        n.f0.accept(this, argu);
        indent--;
        return null;
    }

    public R visit(IntegerType n, A argu) {
        printClassName(n);
        n.f0.accept(this, argu);
        indent--;
        return null;
    }

    public R visit(Statement n, A argu) {
        printClassName(n);
        n.f0.accept(this, argu);
        indent--;
        return null;
    }

    public R visit(Block n, A argu) {
        printClassName(n);
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        indent--;
        return null;
    }

    public R visit(AssignmentStatement n, A argu) {
        printClassName(n);
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        indent--;
        return null;
    }

    public R visit(ArrayAssignmentStatement n, A argu) {
        printClassName(n);
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);
        n.f6.accept(this, argu);
        indent--;
        return null;
    }

    public R visit(IfStatement n, A argu) {
        printClassName(n);
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);
        n.f6.accept(this, argu);
        indent--;
        return null;
    }

    public R visit(WhileStatement n, A argu) {
        printClassName(n);
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        indent--;
        return null;
    }

    public R visit(PrintStatement n, A argu) {
        printClassName(n);
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        indent--;
        return null;
    }

    public R visit(Expression n, A argu) {
        printClassName(n);
        n.f0.accept(this, argu);
        indent--;
        return null;
    }

    public R visit(AndExpression n, A argu) {
        printClassName(n);
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        indent--;
        return null;
    }

    public R visit(CompareExpression n, A argu) {
        printClassName(n);
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        indent--;
        return null;
    }

    public R visit(PlusExpression n, A argu) {
        printClassName(n);
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        indent--;
        return null;
    }

    public R visit(MinusExpression n, A argu) {
        printClassName(n);
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        indent--;
        return null;
    }

    public R visit(TimesExpression n, A argu) {
        printClassName(n);
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        indent--;
        return null;
    }

    public R visit(ArrayLookup n, A argu) {
        printClassName(n);
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        indent--;
        return null;
    }

    public R visit(ArrayLength n, A argu) {
        printClassName(n);
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        indent--;
        return null;
    }

    public R visit(MessageSend n, A argu) {
        printClassName(n);
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);
        indent--;
        return null;
    }

    public R visit(ExpressionList n, A argu) {
        printClassName(n);
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        indent--;
        return null;
    }

    public R visit(ExpressionRest n, A argu) {
        printClassName(n);
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        indent--;
        return null;
    }

    public R visit(PrimaryExpression n, A argu) {
        printClassName(n);
        n.f0.accept(this, argu);
        indent--;
        return null;
    }

    public R visit(IntegerLiteral n, A argu) {
        printClassName(n);
        n.f0.accept(this, argu);
        indent--;
        return null;
    }

    public R visit(TrueLiteral n, A argu) {
        printClassName(n);
        n.f0.accept(this, argu);
        indent--;
        return null;
    }

    public R visit(FalseLiteral n, A argu) {
        printClassName(n);
        n.f0.accept(this, argu);
        indent--;
        return null;
    }

    public R visit(Identifier n, A argu) {
        printClassName(n);
        n.f0.accept(this, argu);
        indent--;
        return null;
    }

    public R visit(ThisExpression n, A argu) {
        printClassName(n);
        n.f0.accept(this, argu);
        indent--;
        return null;
    }

    public R visit(ArrayAllocationExpression n, A argu) {
        printClassName(n);
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        indent--;
        return null;
    }

    public R visit(AllocationExpression n, A argu) {
        printClassName(n);
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        indent--;
        return null;
    }

    public R visit(NotExpression n, A argu) {
        printClassName(n);
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        indent--;
        return null;
    }

    public R visit(BracketExpression n, A argu) {
        printClassName(n);
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        indent--;
        return null;
    }

    private void printClassName(Node node) {
        printIndent();
        System.out.println(node.getClass().getSimpleName());
        indent++;
    }

    private void printIndent() {
        for (int i = 0; i < indent; i++) {
            System.out.print("  ");
        }
    }
}
