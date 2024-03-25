package picojava;

import java.util.HashSet;
import java.util.Set;

import picojava.SymbolTable.ClassBinding;
import picojava.SymbolTable.MethodBinding;
import syntaxtree.*;
import visitor.GJDepthFirst;

public class TypeCheckSimp extends GJDepthFirst<String, SymbolTable> {
    private ClassBinding currentClass;
    private MethodBinding currentMethod;

    public String visit(Goal n, SymbolTable argu) {
        // Distinct: f0, f1 together
        final String mainClassResult = n.f0.accept(this, argu);
        final String classesResult = n.f1.accept(this, argu);

        if (mainClassResult.equals("error") || classesResult.equals("error")) {
            return "error";
        }

        return "";
    }

    public String visit(MainClass n, SymbolTable argu) {
        final String className = n.f1.f0.tokenImage;
        final String mainMethodName = n.f6.tokenImage;

        currentClass = argu.getClassBinding(className);
        currentMethod = currentClass.getMethod(mainMethodName);

        if (n.f14.present()) {
            // Distinct, 0 or more, all are real types
            n.f14.accept(this, argu);
        }

        for (Node statement : n.f15.nodes) {
            System.out.println(statement.toString());
            if (statement.accept(this, argu).equals("error")) {
                return "error";
            }
        }

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

        // Distinct
        n.f3.accept(this, argu);
        // Distinct
        // n.f4.accept(this, argu);

        final String methodsResult = n.f4.accept(this, argu);
        if (methodsResult.equals("error")) {
            return "error";
        }

        n.f5.accept(this, argu);

        currentClass = null;
        return "";
    }

    public String visit(NodeListOptional n, SymbolTable argu) {
        for (Node node : n.nodes) {
            if (node.accept(this, argu).equals("error")) {
                return "error";
            }
        }
        return "";
    }

    @Override
    public String visit(ClassExtendsDeclaration n, SymbolTable argu) {
        final String className = n.f1.f0.tokenImage;
        currentClass = argu.getClassBinding(className);

        // Distinct
        n.f5.accept(this, argu);
        // Distinct, no overloading
        // n.f6.accept(this, argu);
        final String methodsResult = n.f6.accept(this, argu);
        if (methodsResult.equals("error")) {
            return "error";
        }

        currentClass = null;
        return "";
    }

    public String visit(VarDeclaration n, SymbolTable argu) {
        return "";
    }

    @Override
    public String visit(MethodDeclaration n, SymbolTable argu) {
        // TODO: Replace this with currentMethod.getReturnType()?
        final String returnType = n.f1.accept(this, argu);
        final String methodName = n.f2.f0.tokenImage + "()";

        currentMethod = currentClass.getMethod(methodName);
        if (currentMethod == null) {
            return "error";
        }

        // Ensure all parameters have distinct IDs
        // TODO: If this is done in the symbol table, why do it here?
        if (n.f4.present()) {
            FormalParameterList parameters = (FormalParameterList) n.f4.node;
            Set<String> paramSet = new HashSet<>();
            paramSet.add(parameters.f0.f1.f0.toString());
            for (Node parameter : parameters.f1.nodes) {
                parameter.accept(this, argu);
                FormalParameterRest param = (FormalParameterRest) parameter;
                if (!paramSet.add(param.f1.f0.toString())) {
                    return "error";
                }
            }
        }

        // Distinct
        n.f7.accept(this, argu);

        for (Node statement : n.f8.nodes) {
            if (statement.accept(this, argu).equals("error")) {
                return "error";
            }
        }

        final String returnExpression = n.f10.accept(this, argu);
        if (!returnExpression.equals(returnType)) {
            return "error";
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
            case 3:
                return ((Identifier) n.f0.choice).f0.toString();
            default:
                // FIXME: Should we produce an error?
                return "";
        }
    }

    public String visit(Statement n, SymbolTable argu) {
        return n.f0.accept(this, argu);
    }

    public String visit(Block n, SymbolTable argu) {
        if (!n.f1.present()) {
            return "";
        }

        for (Node node : n.f1.nodes) {
            String result = node.accept(this, argu);
            // TODO: Determine whether null result is an error
            if (result == null || result.equals("error")) {
                return "error";
            }
        }

        return "";
    }

    public String visit(AssignmentStatement n, SymbolTable argu) {
        final String lhs = n.f0.f0.tokenImage;
        final String idType = lookupSymbol(lhs, argu);
        final String expressionType = n.f2.accept(this, argu);

        if (isSubtypeOf(expressionType, idType, argu)) {
            return "";
        } else {
            return "error";
        }
    }

    public String visit(ArrayAssignmentStatement n, SymbolTable argu) {
        final String idType = lookupSymbol(n.f0.f0.toString(), argu);
        final String expression1Type = n.f2.accept(this, argu);
        final String expression2Type = n.f5.accept(this, argu);

        if (idType.equals("Int[]") && expression1Type.equals("Int") && expression2Type.equals("Int")) {
            return "";
        } else {
            return "error";
        }
    }

    public String visit(IfStatement n, SymbolTable argu) {
        final String expressionType = n.f2.accept(this, argu);
        final String ifStatementType = n.f4.accept(this, argu);
        final String elseStatementType = n.f4.accept(this, argu);
        if (!expressionType.equals("Boolean")) {
            return "error";
        }

        if (ifStatementType.equals("error") || elseStatementType.equals("error")) {
            return "error";
        }

        return "";
    }

    public String visit(WhileStatement n, SymbolTable argu) {
        final String expressionType = n.f2.accept(this, argu);
        final String statementType = n.f4.accept(this, argu);
        if (expressionType.equals("Boolean") && !statementType.equals("error")) {
            return "";
        } else {
            return "error";
        }
    }

    public String visit(PrintStatement n, SymbolTable argu) {
        final String type = n.f2.accept(this, argu);
        return type.equals("Int") ? "" : "error";
    }

    public String visit(AndExpression n, SymbolTable argu) {
        final String p1Type = n.f0.accept(this, argu);
        final String p2Type = n.f2.accept(this, argu);
        return p1Type.equals("Boolean") && p2Type.equals("Boolean") ? "Boolean" : "error";
    }

    public String visit(CompareExpression n, SymbolTable argu) {
        final String p1Type = n.f0.accept(this, argu);
        final String p2Type = n.f2.accept(this, argu);
        return p1Type.equals("Int") && p2Type.equals("Int") ? "Boolean" : "error";
    }

    public String visit(PlusExpression n, SymbolTable argu) {
        final String p1Type = n.f0.accept(this, argu);
        final String p2Type = n.f2.accept(this, argu);
        return p1Type.equals("Int") && p2Type.equals("Int") ? "Int" : "error";
    }

    public String visit(MinusExpression n, SymbolTable argu) {
        final String p1Type = n.f0.accept(this, argu);
        final String p2Type = n.f2.accept(this, argu);
        return p1Type.equals("Int") && p2Type.equals("Int") ? "Int" : "error";
    }

    public String visit(TimesExpression n, SymbolTable argu) {
        final String p1Type = n.f0.accept(this, argu);
        final String p2Type = n.f2.accept(this, argu);
        return p1Type.equals("Int") && p2Type.equals("Int") ? "Int" : "error";
    }

    public String visit(ArrayLookup n, SymbolTable argu) {
        final String p1Type = n.f0.accept(this, argu);
        final String p2Type = n.f2.accept(this, argu);
        return p1Type.equals("Int[]") && p2Type.equals("Int") ? "Int" : "error";
    }

    public String visit(ArrayLength n, SymbolTable argu) {
        final String type = n.f0.accept(this, argu);
        return type.equals("Int[]") ? "Int" : "error";
    }

    public String visit(MessageSend n, SymbolTable argu) {
        final String className = n.f0.accept(this, argu);
        final String methodName = n.f2.f0.toString() + "()";
        ClassBinding classBinding = argu.getClassBinding(className);

        if (classBinding == null) {
            return "error";
        }
        MethodBinding method = classBinding.getMethod(methodName);
        if (method == null) {
            return "error";
        }
        final String methodType = method.getType();

        if (methodType == null) {
            return "error";
        }

        final String[] methodParts = methodType.split(" \\-\\> ");
        final String[] parameterList = methodParts[0].substring(1, methodParts[0].length() - 1).split(", ");
        final String returnType = methodParts[1];

        String arguments = n.f4.accept(this, argu);
        if (arguments == null) {
            arguments = "";
        }

        if (arguments.equals("error")) {
            return "error";
        }

        final String[] argumentList = arguments.split(", ");

        if (parameterList.length != argumentList.length) {
            return "error";
        }

        for (int i = 0; i < parameterList.length; i++) {
            if (!isSubtypeOf(argumentList[i], parameterList[i], argu)) {
                return "error";
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
            if (type == null || type.equals("error")) {
                return "error";
            }
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
        // final String type = lookupSymbol(currentScope + "." + test, argu);
        final String type = lookupSymbol(test, argu);
        if (type != null) {
            return type;
        } else {
            return "error";
        }
    }

    public String visit(ThisExpression n, SymbolTable argu) {
        if (currentClass != null) {
            return currentClass.getName();
        } else {
            return "error";
        }
    }

    public String visit(ArrayAllocationExpression n, SymbolTable argu) {
        final String type = n.f3.accept(this, argu);

        if (type.equals("Int")) {
            return "Int[]";
        } else {
            return "error";
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
            return "error";
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
