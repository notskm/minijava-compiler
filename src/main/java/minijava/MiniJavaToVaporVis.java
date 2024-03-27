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
    private ClassBinding currentClass = null;

    public String toVapor() {
        String program = "";
        program += methodTablesToVapor();
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
        methodTables.get(currentClass.getName()).add(methodName);
        return null;
    }
}
