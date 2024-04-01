package minijava;

import syntaxtree.*;
import visitor.DepthFirstVisitor;

public class PPrinter extends DepthFirstVisitor {
    private int indent = 0;

    public void visit(NodeToken n) {
        printIndent();
        System.out.print(n.getClass().getSimpleName());
        System.out.print(" => ");
        System.out.println(n.toString());
    }

    public void visit(Goal n) {
        printClassName(n);
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        indent--;
    }

    public void visit(MainClass n) {
        printClassName(n);
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        n.f4.accept(this);
        n.f5.accept(this);
        n.f6.accept(this);
        n.f7.accept(this);
        n.f8.accept(this);
        n.f9.accept(this);
        n.f10.accept(this);
        n.f11.accept(this);
        n.f12.accept(this);
        n.f13.accept(this);
        n.f14.accept(this);
        n.f15.accept(this);
        n.f16.accept(this);
        n.f17.accept(this);
        indent--;
    }

    public void visit(TypeDeclaration n) {
        printClassName(n);
        n.f0.accept(this);
        indent--;
    }

    public void visit(ClassDeclaration n) {
        printClassName(n);
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        n.f4.accept(this);
        n.f5.accept(this);
        indent--;
    }

    public void visit(ClassExtendsDeclaration n) {
        printClassName(n);
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        n.f4.accept(this);
        n.f5.accept(this);
        n.f6.accept(this);
        n.f7.accept(this);
        indent--;
    }

    public void visit(VarDeclaration n) {
        printClassName(n);
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        indent--;
    }

    public void visit(MethodDeclaration n) {
        printClassName(n);
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        n.f4.accept(this);
        n.f5.accept(this);
        n.f6.accept(this);
        n.f7.accept(this);
        n.f8.accept(this);
        n.f9.accept(this);
        n.f10.accept(this);
        n.f11.accept(this);
        n.f12.accept(this);
        indent--;
    }

    public void visit(FormalParameterList n) {
        printClassName(n);
        n.f0.accept(this);
        n.f1.accept(this);
        indent--;
    }

    public void visit(FormalParameter n) {
        printClassName(n);
        n.f0.accept(this);
        n.f1.accept(this);
        indent--;
    }

    public void visit(FormalParameterRest n) {
        printClassName(n);
        n.f0.accept(this);
        n.f1.accept(this);
        indent--;
    }

    public void visit(Type n) {
        printClassName(n);
        n.f0.accept(this);
        indent--;
    }

    public void visit(ArrayType n) {
        printClassName(n);
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        indent--;
    }

    public void visit(BooleanType n) {
        printClassName(n);
        n.f0.accept(this);
        indent--;
    }

    public void visit(IntegerType n) {
        printClassName(n);
        n.f0.accept(this);
        indent--;
    }

    public void visit(Statement n) {
        printClassName(n);
        n.f0.accept(this);
        indent--;
    }

    public void visit(Block n) {
        printClassName(n);
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        indent--;
    }

    public void visit(AssignmentStatement n) {
        printClassName(n);
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        indent--;
    }

    public void visit(ArrayAssignmentStatement n) {
        printClassName(n);
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        n.f4.accept(this);
        n.f5.accept(this);
        n.f6.accept(this);
        indent--;
    }

    public void visit(IfStatement n) {
        printClassName(n);
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        n.f4.accept(this);
        n.f5.accept(this);
        n.f6.accept(this);
        indent--;
    }

    public void visit(WhileStatement n) {
        printClassName(n);
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        n.f4.accept(this);
        indent--;
    }

    public void visit(PrintStatement n) {
        printClassName(n);
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        n.f4.accept(this);
        indent--;
    }

    public void visit(Expression n) {
        printClassName(n);
        n.f0.accept(this);
        indent--;
    }

    public void visit(AndExpression n) {
        printClassName(n);
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        indent--;
    }

    public void visit(CompareExpression n) {
        printClassName(n);
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        indent--;
    }

    public void visit(PlusExpression n) {
        printClassName(n);
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        indent--;
    }

    public void visit(MinusExpression n) {
        printClassName(n);
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        indent--;
    }

    public void visit(TimesExpression n) {
        printClassName(n);
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        indent--;
    }

    public void visit(ArrayLookup n) {
        printClassName(n);
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        indent--;
    }

    public void visit(ArrayLength n) {
        printClassName(n);
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        indent--;
    }

    public void visit(MessageSend n) {
        printClassName(n);
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        n.f4.accept(this);
        n.f5.accept(this);
        indent--;
    }

    public void visit(ExpressionList n) {
        printClassName(n);
        n.f0.accept(this);
        n.f1.accept(this);
        indent--;
    }

    public void visit(ExpressionRest n) {
        printClassName(n);
        n.f0.accept(this);
        n.f1.accept(this);
        indent--;
    }

    public void visit(PrimaryExpression n) {
        printClassName(n);
        n.f0.accept(this);
        indent--;
    }

    public void visit(IntegerLiteral n) {
        printClassName(n);
        n.f0.accept(this);
        indent--;
    }

    public void visit(TrueLiteral n) {
        printClassName(n);
        n.f0.accept(this);
        indent--;
    }

    public void visit(FalseLiteral n) {
        printClassName(n);
        n.f0.accept(this);
        indent--;
    }

    public void visit(Identifier n) {
        printClassName(n);
        n.f0.accept(this);
        indent--;
    }

    public void visit(ThisExpression n) {
        printClassName(n);
        n.f0.accept(this);
        indent--;
    }

    public void visit(ArrayAllocationExpression n) {
        printClassName(n);
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        n.f4.accept(this);
        indent--;
    }

    public void visit(AllocationExpression n) {
        printClassName(n);
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        indent--;
    }

    public void visit(NotExpression n) {
        printClassName(n);
        n.f0.accept(this);
        n.f1.accept(this);
        indent--;
    }

    public void visit(BracketExpression n) {
        printClassName(n);
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        indent--;
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
