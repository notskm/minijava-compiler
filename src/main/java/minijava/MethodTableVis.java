package minijava;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import syntaxtree.*;
import visitor.DepthFirstVisitor;

public class MethodTableVis extends DepthFirstVisitor {
    public Map<String, List<String>> methodTables = new LinkedHashMap<>();

    private String currentClass = "";

    public void visit(MainClass n) {
        currentClass = n.f1.f0.tokenImage;
        n.f14.accept(this);
        n.f15.accept(this);
    }

    public void visit(ClassDeclaration n) {
        currentClass = n.f1.f0.tokenImage;
        addClassToMethodTables(n.f1);
        n.f3.accept(this);
        n.f4.accept(this);
        currentClass = null;
    }

    public void visit(ClassExtendsDeclaration n) {
        currentClass = n.f1.f0.tokenImage;
        addClassToMethodTables(n.f1);
        n.f5.accept(this);
        n.f6.accept(this);
        currentClass = null;
    }

    private void addClassToMethodTables(Identifier classId) {
        methodTables.put(classId.f0.tokenImage, new ArrayList<>());
    }

    public void visit(MethodDeclaration n) {
        final String methodName = n.f2.f0.tokenImage;

        methodTables.get(currentClass).add(methodName);

        n.f2.accept(this);
        n.f4.accept(this);
        n.f7.accept(this);
        n.f8.accept(this);
        n.f10.accept(this);
    }
}
