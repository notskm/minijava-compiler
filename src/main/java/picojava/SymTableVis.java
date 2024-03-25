package picojava;

import picojava.SymbolTable.ClassBinding;
import picojava.SymbolTable.MethodBinding;
import syntaxtree.*;
import visitor.GJDepthFirst;

public class SymTableVis<R, A> extends GJDepthFirst<R, A> {
    public SymbolTable symt = new SymbolTable();

    private String keyPrefix = "Global";
    ClassBinding currentClass = null;
    MethodBinding currentMethod = null;

    @Override
    public R visit(MainClass n, A argu) {
        final String prefix = keyPrefix;
        keyPrefix += "." + n.f1.f0.toString();

        final String className = n.f1.f0.tokenImage;
        final String mainMethodName = n.f6.tokenImage;
        final String argumentName = n.f11.f0.toString();
        final String argumentType = n.f8.toString() + n.f9.toString() + n.f10.toString();
        final String mainType = "(" + argumentType + ") -> " + n.f5.toString();

        symt.addClass(className);
        ClassBinding mainClass = symt.getClassBinding(n.f1.f0.tokenImage);
        mainClass.addMethod(mainMethodName);

        MethodBinding mainMethod = mainClass.getMethod(mainMethodName);
        mainMethod.addParameter(argumentName, argumentType);
        mainMethod.setReturnType(mainType);

        currentClass = mainClass;
        currentMethod = mainMethod;

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

        keyPrefix = prefix;
        currentClass = null;
        currentMethod = null;
        return null;
    }

    @Override
    public R visit(ClassDeclaration n, A argu) {
        final String prefix = keyPrefix;
        keyPrefix += "." + n.f1.f0.toString();

        final String className = n.f1.f0.tokenImage;

        symt.addClass(className);
        currentClass = symt.getClassBinding(className);

        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);

        keyPrefix = prefix;
        currentClass = null;
        return null;
    }

    @Override
    public R visit(ClassExtendsDeclaration n, A argu) {
        String prefix = keyPrefix;
        keyPrefix += "." + n.f1.f0.toString();

        final String className = n.f1.f0.tokenImage;
        final String baseClass = n.f3.f0.tokenImage;

        symt.addClass(className);
        currentClass = symt.getClassBinding(className);
        currentClass.setBaseClass(baseClass);

        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);
        n.f6.accept(this, argu);
        n.f7.accept(this, argu);

        keyPrefix = prefix;
        currentClass = null;

        return null;
    }

    @Override
    public R visit(MethodDeclaration n, A argu) {
        final String methodName = n.f2.f0.toString() + "()";
        final String methodKey = keyPrefix + "." + methodName;

        // FIXME: Error if key already exists

        currentClass.addMethod(methodName);
        currentMethod = currentClass.getMethod(methodName);
        currentMethod.setReturnType(getTypeAsString(n.f1));

        final String prefix = keyPrefix;
        keyPrefix = methodKey;

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

        keyPrefix = prefix;

        currentMethod = null;

        return null;
    }

    @Override
    public R visit(FormalParameter n, A argu) {
        final String type = getTypeAsString(n.f0);
        final String id = n.f1.f0.toString();

        currentMethod.addParameter(id, type);

        return null;
    }

    public R visit(VarDeclaration n, A argu) {
        String type = getTypeAsString(n.f0);

        String id = n.f1.f0.tokenImage;

        if (currentMethod != null) {
            currentMethod.addLocalVariable(id, type);
        } else {
            currentClass.addField(id, type);
        }

        return null;
    }

    public R visit(Block n, A argu) {
        final String prefix = keyPrefix;
        keyPrefix += "." + argu;

        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);

        keyPrefix = prefix;

        return null;
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

    // For testing
    void setPrefix(String prefix) {
        keyPrefix = prefix;
    }
}
