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
        indentLevel++;

        super.visit(n, symt);

        methodString += " ret";

        methods.add(methodString);
        methodString = "";

        indentLevel--;

        return null;
    }

    public Void visit(ClassDeclaration n, SymbolTable symt) {
        currentClass = symt.getClassBinding(n.f1.f0.tokenImage);
        addClassToMethodTables(n.f1);
        super.visit(n, symt);
        currentClass = null;
        return null;
    }

    public Void visit(ClassExtendsDeclaration n, SymbolTable symt) {
        currentClass = symt.getClassBinding(n.f1.f0.tokenImage);
        addClassToMethodTables(n.f1);
        super.visit(n, symt);
        currentClass = null;
        return null;
    }

    private void addClassToMethodTables(Identifier classId) {
        methodTables.put(classId.f0.tokenImage, new ArrayList<>());
    }

    public Void visit(MethodDeclaration n, SymbolTable symt) {
        final String methodName = n.f2.f0.tokenImage;
        final String className = currentClass.getName();

        methodTables.get(currentClass.getName()).add(methodName);

        methodString += "func " + className + "." + methodName + "\n";
        indentLevel++;

        super.visit(n, symt);

        methodString += indent("ret");

        methods.add(methodString);
        methodString = "";

        indentLevel--;

        return null;
    }

    public Void visit(VarDeclaration n, SymbolTable symt) {
        return null;
    }

    public Void visit(AssignmentStatement n, SymbolTable symt) {
        final String variableName = n.f0.f0.tokenImage;
        methodString += indent(variableName + " = ");
        super.visit(n, symt);
        methodString += "\n";
        return null;
    }

    public Void visit(IntegerLiteral n, SymbolTable symt) {
        methodString += n.f0.tokenImage;
        return null;
    }

    private String indent(String str) {
        String indented = "";

        for (int i = 0; i < indentLevel * 2; i++) {
            indented += " ";
        }

        return indented + str;
    }
}
