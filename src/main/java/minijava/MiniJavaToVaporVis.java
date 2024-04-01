package minijava;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import minijava.SymbolTable.ClassBinding;
import syntaxtree.*;
import visitor.GJDepthFirst;

public class MiniJavaToVaporVis extends GJDepthFirst<Void, SymbolTable> {
    public Map<String, List<String>> methodTables;
    public List<String> methods = new ArrayList<>();

    private ClassBinding currentClass = null;
    private String methodString = "";
    private int indentLevel = 0;
    private int tempVariableNumber = 0;
    private int ifLabelNumber = 1;
    private int nullLabelNumber = 1;

    public MiniJavaToVaporVis(Map<String, List<String>> methodTable) {
        methodTables = methodTable;
    }

    public String toVapor() {
        String program = "\n";
        program += methodTablesToVapor();
        program += "\n";
        program += methodsToVapor();
        program += "\n";
        return program;
    }

    public String methodTablesToVapor() {
        String tables = "";

        for (Entry<String, List<String>> entry : methodTables.entrySet()) {
            final String className = entry.getKey();
            final List<String> methods = entry.getValue();
            String table = methodTableToVapor(className, methods);
            tables += table + "\n";
        }

        return tables;
    }

    public String methodTableToVapor(String className, List<String> methods) {
        String table = "const vmt_" + className + "\n";
        for (String method : methods) {
            table += "  :" + className + "." + method + "\n";
        }

        return table;
    }

    public String methodsToVapor() {
        return String.join("\n\n", methods);
    }

    public Void visit(MainClass n, SymbolTable symt) {
        currentClass = symt.getClassBinding(n.f1.f0.tokenImage);
        methodString += "func Main()\n";
        beginScope();
        n.f14.accept(this, symt);
        n.f15.accept(this, symt);
        methodString += indent("ret");

        methods.add(methodString);
        methodString = "";

        endScope();

        return null;
    }

    public Void visit(ClassDeclaration n, SymbolTable symt) {
        currentClass = symt.getClassBinding(n.f1.f0.tokenImage);
        n.f3.accept(this, symt);
        n.f4.accept(this, symt);
        currentClass = null;
        return null;
    }

    public Void visit(ClassExtendsDeclaration n, SymbolTable symt) {
        currentClass = symt.getClassBinding(n.f1.f0.tokenImage);
        n.f5.accept(this, symt);
        n.f6.accept(this, symt);
        currentClass = null;
        return null;
    }

    public Void visit(MethodDeclaration n, SymbolTable symt) {
        final String className = currentClass.getName();
        final String methodName = n.f2.f0.tokenImage;

        n.f4.accept(this, symt);
        String arguments = "this";

        if (!arguments.isEmpty()) {
            arguments += " " + expressionVariable;
        }

        methodString += "func " + className + "." + methodName + "(" + arguments + ")\n";

        beginScope();

        n.f7.accept(this, symt);
        n.f8.accept(this, symt);

        n.f10.accept(this, symt);
        final String returnVar = expressionVariable;
        methodString += indent("ret " + returnVar);

        methods.add(methodString);
        methodString = "";

        endScope();

        return null;
    }

    private String expressionVariable = "";
    private String expressionVariableType = "";

    public Void visit(AllocationExpression n, SymbolTable argu) {
        final String className = n.f1.f0.tokenImage;
        final int bytes = argu.getClassBinding(className).getSizeInBytes() + 4;
        final String tempVar = newTempVariable();
        final String nullLabel = "null" + nullLabelNumber;

        nullLabelNumber++;

        methodString += indent(tempVar + " = HeapAllocZ(" + bytes + ")\n");
        methodString += indent("[" + tempVar + "] = :vmt_" + className + "\n");
        methodString += indent("if " + tempVar + " goto :" + nullLabel + "\n");
        beginIndent();
        methodString += indent("Error(\"null pointer\")\n");
        endIndent();
        methodString += indent("null1:\n");

        expressionVariable = tempVar;
        expressionVariableType = className;

        return null;
    }

    public Void visit(VarDeclaration n, SymbolTable symt) {
        return null;
    }

    public Void visit(AssignmentStatement n, SymbolTable symt) {
        n.f2.accept(this, symt);

        methodString += indent(n.f0.f0.tokenImage + " = " + expressionVariable);
        methodString += "\n";
        return null;
    }

    public Void visit(IntegerLiteral n, SymbolTable symt) {
        expressionVariable = n.f0.tokenImage;
        expressionVariableType = "Int";
        return null;
    }

    public Void visit(Identifier n, SymbolTable symt) {
        expressionVariable = n.f0.tokenImage;
        expressionVariableType = "";
        return null;
    }

    public Void visit(IfStatement n, SymbolTable symt) {
        final String elseLabel = "if" + ifLabelNumber + "_else";
        final String endLabel = "if" + ifLabelNumber + "_end";

        ifLabelNumber++;

        n.f2.accept(this, symt);

        methodString += indent("if0 " + expressionVariable + " goto :" + elseLabel + "\n");
        beginIndent();
        n.f4.accept(this, symt);
        methodString += indent("goto :" + endLabel + "\n");
        endIndent();

        methodString += indent(elseLabel + ":\n");
        beginIndent();
        n.f6.accept(this, symt);
        endIndent();

        methodString += indent(endLabel + ":\n");

        return null;
    }

    public Void visit(PrimaryExpression n, SymbolTable symt) {
        return n.f0.accept(this, symt);
    }

    public Void visit(ThisExpression n, SymbolTable symt) {
        expressionVariable = "this";
        expressionVariableType = currentClass.getName();
        return null;
    }

    public Void visit(ExpressionList n, SymbolTable symt) {
        n.f0.accept(this, symt);
        final String before = expressionVariable;
        expressionVariable = "";

        n.f1.accept(this, symt);
        final String after = expressionVariable;
        expressionVariable = before;

        if (!after.isEmpty()) {
            expressionVariable += after;
        }

        expressionVariableType = "";

        return null;
    }

    public Void visit(ExpressionRest n, SymbolTable symt) {
        final String before = expressionVariable;
        expressionVariable = "";

        n.f1.accept(this, symt);
        final String after = expressionVariable;
        expressionVariable = before;

        if (!after.isEmpty()) {
            expressionVariable += " " + expressionVariable;
        }

        return null;
    }

    public Void visit(MessageSend n, SymbolTable symt) {
        n.f0.accept(this, symt);

        final String expressionVar = expressionVariable;
        final String methodName = n.f2.f0.tokenImage;
        final int methodIndex = methodTables.get(expressionVariableType).indexOf(methodName);
        final String methodVar = newTempVariable();

        methodString += indent(methodVar + " = [" + expressionVar + "]\n");
        methodString += indent(methodVar + " = [" + methodVar + "+" + methodIndex + "]\n");

        n.f4.accept(this, symt);
        String arguments = expressionVar;
        if (!expressionVariable.isEmpty()) {
            arguments += " " + expressionVariable;
        }

        final String tempVar = newTempVariable();
        methodString += indent(tempVar + " = call " + methodVar + "(" + arguments + ")\n");
        expressionVariable = tempVar;

        return null;
    }

    public Void visit(CompareExpression n, SymbolTable symt) {
        n.f0.accept(this, symt);
        final String lhs = expressionVariable;

        n.f2.accept(this, symt);
        final String rhs = expressionVariable;

        final String tempVar = newTempVariable();
        methodString += indent(tempVar + " = LtS(" + lhs + " " + rhs + ")\n");

        expressionVariable = tempVar;
        expressionVariableType = "Boolean";

        return null;
    }

    public Void visit(MinusExpression n, SymbolTable symt) {
        n.f0.accept(this, symt);
        final String lhs = expressionVariable;

        n.f2.accept(this, symt);
        final String rhs = expressionVariable;

        final String tempVar = newTempVariable();
        methodString += indent(tempVar + " = Sub(" + lhs + " " + rhs + ")\n");

        expressionVariable = tempVar;

        return null;
    }

    public Void visit(TimesExpression n, SymbolTable symt) {
        n.f0.accept(this, symt);
        final String lhs = expressionVariable;

        n.f2.accept(this, symt);
        final String rhs = expressionVariable;

        expressionVariable = "MulS(" + lhs + " " + rhs + ")";
        expressionVariableType = "Boolean";

        return null;
    }

    public Void visit(PrintStatement n, SymbolTable symt) {
        n.f2.accept(this, symt);
        methodString += indent("PrintIntS(" + expressionVariable + ")\n");
        return null;
    }

    private String indent(String str) {
        String indented = "";

        for (int i = 0; i < indentLevel * 2; i++) {
            indented += " ";
        }

        return indented + str;
    }

    private String newTempVariable() {
        final String temp = "t." + tempVariableNumber;
        tempVariableNumber++;
        return temp;
    }

    private void beginIndent() {
        indentLevel++;
    }

    private void endIndent() {
        indentLevel--;
    }

    private void beginScope() {
        beginIndent();
    }

    private void endScope() {
        tempVariableNumber = 0;
        ifLabelNumber = 1;
        endIndent();
    }
}
