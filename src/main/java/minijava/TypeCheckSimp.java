package minijava;

import minijava.SymbolTable.ClassBinding;
import minijava.SymbolTable.MethodBinding;
import syntaxtree.*;
import visitor.GJDepthFirst;

public class TypeCheckSimp extends GJDepthFirst<String, SymbolTable> {
    private ClassBinding currentClass;
    private MethodBinding currentMethod;

    public String visit(Goal n, SymbolTable argu) {
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        return "";
    }

    public String visit(MainClass n, SymbolTable argu) {
        final String className = n.f1.f0.tokenImage;
        final String mainMethodName = n.f6.tokenImage;

        currentClass = argu.getClassBinding(className);
        currentMethod = currentClass.getMethod(mainMethodName);

        n.f14.accept(this, argu);

        n.f15.accept(this, argu);
        n.f16.accept(this, argu);
        n.f17.accept(this, argu);

        currentMethod = null;
        currentClass = null;

        return "";
    }

    @Override
    public String visit(TypeDeclaration n, SymbolTable argu) {
        return n.f0.accept(this, argu);
    }

    @Override
    public String visit(ClassDeclaration n, SymbolTable argu) {
        final String className = n.f1.f0.tokenImage;
        currentClass = argu.getClassBinding(className);

        n.f3.accept(this, argu);
        n.f4.accept(this, argu);

        currentClass = null;
        return "";
    }

    public String visit(NodeListOptional n, SymbolTable argu) {
        for (Node node : n.nodes) {
            node.accept(this, argu);
        }
        return "";
    }

    @Override
    public String visit(ClassExtendsDeclaration n, SymbolTable argu) {
        final String className = n.f1.f0.tokenImage;
        currentClass = argu.getClassBinding(className);

        n.f5.accept(this, argu);

        for (MethodBinding method : currentClass.getMethods()) {
            final String methodName = method.getName();
            final String methodType = method.getType();
            final String parentName = currentClass.getBaseClass();
            final ClassBinding parent = argu.getClassBinding(parentName);
            final MethodBinding parentMethod = parent.getMethod(methodName);
            if (parentMethod != null && !parentMethod.getType().equals(methodType)) {
                throw new TypecheckException("Overloading methods is not allowed");
            }
        }

        n.f6.accept(this, argu);

        currentClass = null;
        return "";
    }

    public String visit(VarDeclaration n, SymbolTable argu) {
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        return "";
    }

    @Override
    public String visit(MethodDeclaration n, SymbolTable argu) {
        final String returnType = n.f1.accept(this, argu);
        final String methodName = n.f2.f0.tokenImage + "()";

        currentMethod = currentClass.getMethod(methodName);
        if (currentMethod == null) {
            throw new TypecheckException("Method does not exist");
        }

        n.f7.accept(this, argu);
        n.f8.accept(this, argu);

        final String returnExpression = n.f10.accept(this, argu);
        if (!returnExpression.equals(returnType)) {
            throw new TypecheckException("Expected " + returnType + " got " + returnExpression);
        }

        currentMethod = null;

        return "";
    }

    public String visit(NodeChoice n, SymbolTable argu) {
        return n.choice.accept(this, argu);
    }

    public String visit(Expression n, SymbolTable argu) {
        return n.f0.accept(this, argu);
    }

    public String visit(Type n, SymbolTable argu) {
        switch (n.f0.which) {
            case 0:
                return "Int[]";
            case 1:
                return "Boolean";
            case 2:
                return "Int";
            default:
                final Identifier type = (Identifier) n.f0.choice;
                final String typeName = type.f0.tokenImage;
                final ClassBinding binding = argu.getClassBinding(typeName);

                if (binding == null) {
                    throw new TypecheckException("Invalid type " + typeName);
                }

                return ((Identifier) n.f0.choice).f0.toString();
        }
    }

    public String visit(Statement n, SymbolTable argu) {
        return n.f0.accept(this, argu);
    }

    public String visit(Block n, SymbolTable argu) {
        n.f1.accept(this, argu);
        return "";
    }

    public String visit(AssignmentStatement n, SymbolTable argu) {
        final String lhs = n.f0.f0.tokenImage;
        final String idType = lookupSymbol(lhs, argu);
        final String expressionType = n.f2.accept(this, argu);

        if (isSubtypeOf(expressionType, idType, argu)) {
            return "";
        } else {
            throw new TypecheckException("Expected " + idType + ", got " + expressionType);
        }
    }

    public String visit(ArrayAssignmentStatement n, SymbolTable argu) {
        final String idType = lookupSymbol(n.f0.f0.toString(), argu);
        final String expression1Type = n.f2.accept(this, argu);
        final String expression2Type = n.f5.accept(this, argu);

        if (!idType.equals("Int[]")) {
            throw new TypecheckException("Expected int[], got " + idType);
        }
        if (!expression1Type.equals("Int")) {
            throw new TypecheckException("expected int, got " + expression1Type);
        }
        if (!expression2Type.equals("Int")) {
            throw new TypecheckException("expected int, got " + expression2Type);
        }
        return "";
    }

    public String visit(IfStatement n, SymbolTable argu) {
        final String expressionType = n.f2.accept(this, argu);
        n.f4.accept(this, argu);
        n.f4.accept(this, argu);

        if (!expressionType.equals("Boolean")) {
            throw new TypecheckException("Expected boolean, got " + expressionType);
        }

        return "";
    }

    public String visit(WhileStatement n, SymbolTable argu) {
        final String expressionType = n.f2.accept(this, argu);
        n.f4.accept(this, argu);
        if (expressionType.equals("Boolean")) {
            return "";
        } else {
            throw new TypecheckException("Expected boolean, got " + expressionType);
        }
    }

    public String visit(PrintStatement n, SymbolTable argu) {
        final String type = n.f2.accept(this, argu);
        if (!type.equals("Int")) {
            throw new TypecheckException("Expected int, got " + type);
        }
        return "";
    }

    public String visit(AndExpression n, SymbolTable argu) {
        final String p1Type = n.f0.accept(this, argu);
        final String p2Type = n.f2.accept(this, argu);
        if (!p1Type.equals("Boolean")) {
            throw new TypecheckException("Expected boolean, got " + p1Type);
        }
        if (!p2Type.equals("Boolean")) {
            throw new TypecheckException("Expected boolean, got " + p2Type);
        }
        return "Boolean";
    }

    public String visit(CompareExpression n, SymbolTable argu) {
        final String p1Type = n.f0.accept(this, argu);
        final String p2Type = n.f2.accept(this, argu);
        if (!p1Type.equals("Int")) {
            throw new TypecheckException("Expected int, got " + p1Type);
        }
        if (!p2Type.equals("Int")) {
            throw new TypecheckException("Expected int, got " + p2Type);
        }
        return "Boolean";
    }

    public String visit(PlusExpression n, SymbolTable argu) {
        final String p1Type = n.f0.accept(this, argu);
        final String p2Type = n.f2.accept(this, argu);
        if (!p1Type.equals("Int")) {
            throw new TypecheckException("Expected int, got " + p1Type);
        }
        if (!p2Type.equals("Int")) {
            throw new TypecheckException("Expected int, got " + p2Type);
        }
        return "Int";
    }

    public String visit(MinusExpression n, SymbolTable argu) {
        final String p1Type = n.f0.accept(this, argu);
        final String p2Type = n.f2.accept(this, argu);
        if (!p1Type.equals("Int")) {
            throw new TypecheckException("Expected int, got " + p1Type);
        }
        if (!p2Type.equals("Int")) {
            throw new TypecheckException("Expected int, got " + p2Type);
        }
        return "Int";
    }

    public String visit(TimesExpression n, SymbolTable argu) {
        final String p1Type = n.f0.accept(this, argu);
        final String p2Type = n.f2.accept(this, argu);
        if (!p1Type.equals("Int")) {
            throw new TypecheckException("Expected int, got " + p1Type);
        }
        if (!p2Type.equals("Int")) {
            throw new TypecheckException("Expected int, got " + p2Type);
        }
        return "Int";
    }

    public String visit(ArrayLookup n, SymbolTable argu) {
        final String p1Type = n.f0.accept(this, argu);
        final String p2Type = n.f2.accept(this, argu);
        if (p1Type.equals("Int[]") && p2Type.equals("Int")) {
            return "Int";
        } else {
            throw new TypecheckException("Expected int, got " + p2Type);
        }
    }

    public String visit(ArrayLength n, SymbolTable argu) {
        final String type = n.f0.accept(this, argu);
        if (!type.equals("Int[]")) {
            throw new TypecheckException("Expected int[], got " + type);
        }
        return "Int";
    }

    public String visit(MessageSend n, SymbolTable argu) {
        final String className = n.f0.accept(this, argu);
        final String methodName = n.f2.f0.toString() + "()";
        ClassBinding classBinding = argu.getClassBinding(className);

        if (classBinding == null) {
            throw new TypecheckException("Class does not exist");
        }
        MethodBinding method = classBinding.getMethod(methodName);
        if (method == null) {
            throw new TypecheckException("Method does not exist");
        }
        final String methodType = method.getType();

        if (methodType == null) {
            throw new TypecheckException("Method has no type information");
        }

        final String[] methodParts = methodType.split(" \\-\\> ");
        final String[] parameterList = methodParts[0].substring(1, methodParts[0].length() - 1).split(", ");
        final String returnType = methodParts[1];

        String arguments = n.f4.accept(this, argu);
        if (arguments == null) {
            arguments = "";
        }

        final String[] argumentList = arguments.split(", ");

        if (parameterList.length != argumentList.length) {
            throw new TypecheckException("Argument list size does not match parameter list size");
        }

        for (int i = 0; i < parameterList.length; i++) {
            if (!isSubtypeOf(argumentList[i], parameterList[i], argu)) {
                throw new TypecheckException("Invalid argument type");
            }
        }

        return returnType;
    }

    public String visit(ExpressionList n, SymbolTable argu) {
        String list = n.f0.accept(this, argu);
        if (list == null) {
            list = "";
        }

        for (Node expr : n.f1.nodes) {
            final String type = expr.accept(this, argu);
            list += ", " + type;
        }

        return list;
    }

    public String visit(ExpressionRest n, SymbolTable argu) {
        return n.f1.accept(this, argu);
    }

    public String visit(PrimaryExpression n, SymbolTable argu) {
        return n.f0.accept(this, argu);
    }

    public String visit(IntegerLiteral n, SymbolTable argu) {
        return "Int";
    }

    public String visit(TrueLiteral n, SymbolTable argu) {
        return "Boolean";
    }

    public String visit(FalseLiteral n, SymbolTable argu) {
        return "Boolean";
    }

    public String visit(Identifier n, SymbolTable argu) {
        final String test = n.f0.toString();
        final String type = lookupSymbol(test, argu);
        if (type != null) {
            return type;
        } else {
            throw new TypecheckException("Type information for identifier not found");
        }
    }

    public String visit(ThisExpression n, SymbolTable argu) {
        if (currentClass != null) {
            return currentClass.getName();
        } else {
            throw new TypecheckException("this used outside of a class");
        }
    }

    public String visit(ArrayAllocationExpression n, SymbolTable argu) {
        final String type = n.f3.accept(this, argu);

        if (type.equals("Int")) {
            return "Int[]";
        } else {
            throw new TypecheckException("Expression assigned to int[] is not of type int[]");
        }
    }

    public String visit(AllocationExpression n, SymbolTable argu) {
        return n.f1.f0.toString();
    }

    public String visit(NotExpression n, SymbolTable argu) {
        final String type = n.f1.accept(this, argu);
        if (type.equals("Boolean")) {
            return type;
        } else {
            throw new TypecheckException("Not expression does not evaluation to boolean");
        }
    }

    public String visit(BracketExpression n, SymbolTable argu) {
        return n.f1.accept(this, argu);
    }

    private String lookupSymbol(String name, SymbolTable symt) {
        String type = null;

        if (currentMethod != null) {
            type = currentMethod.lookup(name);
        }

        if (type == null && currentClass != null) {
            type = currentClass.lookup(name);
        }

        return type;
    }

    private boolean isSubtypeOf(String derived, String base, SymbolTable symt) {
        if (derived == null) {
            return false;
        }

        if (derived.equals(base)) {
            return true;
        }

        final ClassBinding baseClass = symt.getClassBinding(derived);
        if (baseClass != null) {
            return isSubtypeOf(baseClass.getBaseClass(), base, symt);
        } else {
            return false;
        }
    }
}
