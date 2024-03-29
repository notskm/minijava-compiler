package minijava;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import minijava.SymbolTable.ClassBinding;
import syntaxtree.*;
import visitor.GJDepthFirst;

public class MiniJavaToVaporVis extends GJDepthFirst<Void, SymbolTable> {
    public Map<String, List<String>> methodTables = new LinkedHashMap<>();
    public List<String> methods = new ArrayList<>();

    private ClassBinding currentClass = null;
    private String methodString = "";
    private int indentLevel = 0;
    private int tempVariableNumber = 0;
    private int ifLabelNumber = 1;
    private int nullLabelNumber = 1;

    public String toVapor() {
        String program = "";
        program += methodTablesToVapor();
        program += methodsToVapor();
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
        methodString += "\n";
        methodString += "ret";

        methods.add(methodString);
        methodString = "";

        endScope();

        return null;
    }

    public Void visit(ClassDeclaration n, SymbolTable symt) {
        currentClass = symt.getClassBinding(n.f1.f0.tokenImage);
        addClassToMethodTables(n.f1);
        n.f3.accept(this, symt);
        n.f4.accept(this, symt);
        currentClass = null;
        return null;
    }

    public Void visit(ClassExtendsDeclaration n, SymbolTable symt) {
        currentClass = symt.getClassBinding(n.f1.f0.tokenImage);
        addClassToMethodTables(n.f1);
        n.f5.accept(this, symt);
        n.f6.accept(this, symt);
        currentClass = null;
        return null;
    }

    private void addClassToMethodTables(Identifier classId) {
        methodTables.put(classId.f0.tokenImage, new ArrayList<>());
    }

    public Void visit(MethodDeclaration n, SymbolTable symt) {
        final String className = currentClass.getName();
        final String methodName = n.f2.f0.tokenImage;

        methodTables.get(currentClass.getName()).add(methodName);

        methodString += "func " + className + ".";
        n.f2.accept(this, symt);
        methodString += "(this ";
        n.f4.accept(this, symt);
        methodString += ")\n";

        beginScope();

        n.f7.accept(this, symt);
        n.f8.accept(this, symt);

        methodString += indent("ret ");

        n.f10.accept(this, symt);

        methods.add(methodString);
        methodString = "";

        endScope();

        return null;
    }

    public Void visit(AllocationExpression n, SymbolTable argu) {
        final String className = n.f1.f0.tokenImage;
        final int bytes = argu.getClassBinding(className).getSizeInBytes() + 4;
        final String tempVar = "t." + tempVariableNumber;
        final String nullLabel = "null" + nullLabelNumber;

        tempVariableNumber++;
        nullLabelNumber++;

        methodString += indent(tempVar + " = HeapAllocZ(" + bytes + ")\n");
        methodString += indent("[" + tempVar + "] = :vmt_" + className + "\n");
        methodString += indent("if " + tempVar + " goto :" + nullLabel + "\n");
        beginIndent();
        methodString += indent("Error(\"null pointer\")\n");
        endIndent();
        methodString += indent("null1:\n");

        return null;
    }

    public Void visit(VarDeclaration n, SymbolTable symt) {
        return null;
    }

    public Void visit(AssignmentStatement n, SymbolTable symt) {
        methodString += indent("");
        n.f0.accept(this, symt);
        methodString += " = ";
        n.f2.accept(this, symt);
        methodString += "\n";
        return null;
    }

    public Void visit(IntegerLiteral n, SymbolTable symt) {
        methodString += n.f0.tokenImage;
        return null;
    }

    public Void visit(Identifier n, SymbolTable symt) {
        methodString += n.f0.tokenImage;
        return null;
    }

    public Void visit(IfStatement n, SymbolTable symt) {
        final String temp = "t." + tempVariableNumber;
        final String elseLabel = "if" + ifLabelNumber + "_else";
        final String endLabel = "if" + ifLabelNumber + "_end";

        ifLabelNumber++;

        methodString += indent(temp + " = ");
        n.f2.accept(this, symt);
        methodString += "\n";

        methodString += indent("if0 " + temp + " goto :" + elseLabel + "\n");
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

    public Void visit(CompareExpression n, SymbolTable symt) {
        methodString += "LtS(";
        n.f0.accept(this, symt);
        methodString += " ";
        n.f2.accept(this, symt);
        methodString += ")";

        return null;
    }

    private String indent(String str) {
        String indented = "";

        for (int i = 0; i < indentLevel * 2; i++) {
            indented += " ";
        }

        return indented + str;
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
