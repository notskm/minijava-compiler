package minijava;

import minijava.SymbolTable.ClassBinding;
import minijava.SymbolTable.MethodBinding;
import syntaxtree.*;
import visitor.DepthFirstVisitor;

public class SymTableVis extends DepthFirstVisitor {
    public SymbolTable symt = new SymbolTable();

    ClassBinding currentClass = null;
    MethodBinding currentMethod = null;

    @Override
    public void visit(MainClass n) {
        final String className = n.f1.f0.tokenImage;
        final String mainMethodName = n.f6.tokenImage;
        final String argumentName = n.f11.f0.toString();
        final String argumentType = n.f8.toString() + n.f9.toString() + n.f10.toString();
        final String mainType = "(" + argumentType + ") -> " + n.f5.toString();

        if (!symt.addClass(className)) {
            throw new TypecheckException("Class already exists");
        }

        ClassBinding mainClass = symt.getClassBinding(n.f1.f0.tokenImage);
        mainClass.addMethod(mainMethodName);

        MethodBinding mainMethod = mainClass.getMethod(mainMethodName);
        mainMethod.addParameter(argumentName, argumentType);
        mainMethod.setReturnType(mainType);

        currentClass = mainClass;
        currentMethod = mainMethod;

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

        currentClass = null;
        currentMethod = null;
    }

    @Override
    public void visit(ClassDeclaration n) {
        final String className = n.f1.f0.tokenImage;

        if (!symt.addClass(className)) {
            throw new TypecheckException("Class already exists");
        }
        currentClass = symt.getClassBinding(className);

        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        n.f4.accept(this);
        n.f5.accept(this);

        currentClass = null;
    }

    @Override
    public void visit(ClassExtendsDeclaration n) {
        final String className = n.f1.f0.tokenImage;
        final String baseClass = n.f3.f0.tokenImage;

        if (!symt.addClass(className)) {
            throw new TypecheckException("Class already exists");
        }

        currentClass = symt.getClassBinding(className);
        currentClass.setBaseClass(baseClass);

        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        n.f4.accept(this);
        n.f5.accept(this);
        n.f6.accept(this);
        n.f7.accept(this);

        currentClass = null;
    }

    @Override
    public void visit(MethodDeclaration n) {
        final String methodName = n.f2.f0.toString() + "()";

        if (!currentClass.addMethod(methodName)) {
            throw new TypecheckException("Method already exists");
        }

        currentMethod = currentClass.getMethod(methodName);
        currentMethod.setReturnType(getTypeAsString(n.f1));

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

        currentMethod = null;
    }

    @Override
    public void visit(FormalParameter n) {
        final String type = getTypeAsString(n.f0);
        final String id = n.f1.f0.toString();
        if (!currentMethod.addParameter(id, type)) {
            throw new TypecheckException("Parameter already exists");
        }
    }

    public void visit(VarDeclaration n) {
        String type = getTypeAsString(n.f0);

        String id = n.f1.f0.tokenImage;

        if (currentMethod != null) {
            if (!currentMethod.addLocalVariable(id, type)) {
                throw new TypecheckException("local variable already exists");
            }
        } else {
            if (!currentClass.addField(id, type)) {
                throw new TypecheckException("Class field already exists");
            }
        }
    }

    public void visit(Block n) {
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
    }

    private String getTypeAsString(Type n) {
        switch (n.f0.which) {
            case 0:
                return "Int[]";
            case 1:
                return "Boolean";
            case 2:
                return "Int";
            case 3:
                return ((Identifier) n.f0.choice).f0.toString();
            default:
                // FIXME: Should we produce an error?
                return "";
        }
    }
}
