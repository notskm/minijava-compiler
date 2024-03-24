package picojava;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import syntaxtree.*;
import visitor.GJDepthFirst;

public class TypeCheckSimp extends GJDepthFirst<String, HashMap<String, String>> {
    private String currentClass = "";
    private String currentScope = "Global";

    public String visit(Goal n, HashMap<String, String> argu) {
        // Distinct: f0, f1 together
        final String mainClassResult = n.f0.accept(this, argu);
        final String classesResult = n.f1.accept(this, argu);

        if (mainClassResult.equals("error") || classesResult.equals("error")) {
            return "error";
        }

        return "";
    }

    public String visit(MainClass n, HashMap<String, String> argu) {
        final String scope = currentScope;
        currentScope += "." + n.f1.f0.toString();
        currentClass = n.f1.f0.toString();

        // n.f0.accept(this, argu);
        // n.f1.accept(this, argu);
        // n.f2.accept(this, argu);
        // n.f3.accept(this, argu);
        // n.f4.accept(this, argu);
        // n.f5.accept(this, argu);
        // n.f6.accept(this, argu);
        // n.f7.accept(this, argu);
        // n.f8.accept(this, argu);
        // n.f9.accept(this, argu);
        // n.f10.accept(this, argu);
        // n.f11.accept(this, argu);
        // n.f12.accept(this, argu);
        // n.f13.accept(this, argu);

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

        currentScope = scope;
        return "";
    }

    @Override
    public String visit(TypeDeclaration n, HashMap<String, String> argu) {
        return n.f0.accept(this, argu);
    }

    @Override
    public String visit(ClassDeclaration n, HashMap<String, String> argu) {
        final String prefix = currentScope;
        currentScope += "." + n.f1.f0.toString();
        currentClass = n.f1.f0.toString();

        // n.f0.accept(this, argu);
        // n.f1.accept(this, argu);
        // n.f2.accept(this, argu);
        // Distinct
        n.f3.accept(this, argu);
        // Distinct
        // n.f4.accept(this, argu);

        final String methodsResult = n.f4.accept(this, argu);
        if (methodsResult.equals("error")) {
            return "error";
        }

        n.f5.accept(this, argu);

        currentScope = prefix;
        return "";
    }

    public String visit(NodeListOptional n, HashMap<String, String> argu) {
        for (Node node : n.nodes) {
            if (node.accept(this, argu).equals("error")) {
                return "error";
            }
        }
        return "";
    }

    @Override
    public String visit(ClassExtendsDeclaration n, HashMap<String, String> argu) {
        String prefix = currentScope;
        currentScope += "." + n.f1.f0.toString();
        currentClass = n.f1.f0.toString();

        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        // Distinct
        n.f5.accept(this, argu);
        // Distinct, no overloading
        // n.f6.accept(this, argu);
        final String methodsResult = n.f6.accept(this, argu);
        if (methodsResult.equals("error")) {
            return "error";
        }
        n.f7.accept(this, argu);

        currentScope = prefix;
        return "";
    }

    public String visit(VarDeclaration n, HashMap<String, String> argu) {
        return "";
    }

    @Override
    public String visit(MethodDeclaration n, HashMap<String, String> argu) {
        n.f0.accept(this, argu);

        final String returnType = n.f1.accept(this, argu);

        final String methodName = n.f2.f0.toString() + "()";
        if (lookupSymbol(methodName, argu) == null) {
            return "error";
        }

        final String methodKey = currentScope + "." + methodName;
        final String prefix = currentScope;
        currentScope = methodKey;

        n.f3.accept(this, argu);

        // Ensure all parameters have distinct IDs
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

        n.f5.accept(this, argu);
        n.f6.accept(this, argu);
        // Distinct
        n.f7.accept(this, argu);

        for (Node statement : n.f8.nodes) {
            if (statement.accept(this, argu).equals("error")) {
                return "error";
            }
        }

        n.f9.accept(this, argu);
        final String returnExpression = n.f10.accept(this, argu);
        if (!returnExpression.equals(returnType)) {
            return "error";
        }
        n.f11.accept(this, argu);
        n.f12.accept(this, argu);

        currentScope = prefix;

        return "";
    }

    public String visit(NodeChoice n, HashMap<String, String> argu) {
        return n.choice.accept(this, argu);
    }

    public String visit(Expression n, HashMap<String, String> argu) {
        return n.f0.accept(this, argu);
    }

    public String visit(Type n, HashMap<String, String> argu) {
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

    public String visit(Statement n, HashMap<String, String> argu) {
        return n.f0.accept(this, argu);
    }

    public String visit(Block n, HashMap<String, String> argu) {
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

    public String visit(AssignmentStatement n, HashMap<String, String> argu) {
        final String idType = lookupSymbol(n.f0.f0.toString(), argu);
        final String expressionType = n.f2.accept(this, argu);

        if (isSubtypeOf(expressionType, idType, argu)) {
            return "";
        } else {
            return "error";
        }
    }

    public String visit(ArrayAssignmentStatement n, HashMap<String, String> argu) {
        final String idType = lookupSymbol(n.f0.f0.toString(), argu);
        final String expression1Type = n.f2.accept(this, argu);
        final String expression2Type = n.f5.accept(this, argu);

        if (idType.equals("Int[]") && expression1Type.equals("Int") && expression2Type.equals("Int")) {
            return "";
        } else {
            return "error";
        }
    }

    public String visit(IfStatement n, HashMap<String, String> argu) {
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

    public String visit(WhileStatement n, HashMap<String, String> argu) {
        final String expressionType = n.f2.accept(this, argu);
        final String statementType = n.f4.accept(this, argu);
        if (expressionType.equals("Boolean") && !statementType.equals("error")) {
            return "";
        } else {
            return "error";
        }
    }

    public String visit(PrintStatement n, HashMap<String, String> argu) {
        final String type = n.f2.accept(this, argu);
        return type.equals("Int") ? "" : "error";
    }

    public String visit(AndExpression n, HashMap<String, String> argu) {
        final String p1Type = n.f0.accept(this, argu);
        final String p2Type = n.f2.accept(this, argu);
        return p1Type.equals("Boolean") && p2Type.equals("Boolean") ? "Boolean" : "error";
    }

    public String visit(CompareExpression n, HashMap<String, String> argu) {
        final String p1Type = n.f0.accept(this, argu);
        final String p2Type = n.f2.accept(this, argu);
        return p1Type.equals("Int") && p2Type.equals("Int") ? "Boolean" : "error";
    }

    public String visit(PlusExpression n, HashMap<String, String> argu) {
        final String p1Type = n.f0.accept(this, argu);
        final String p2Type = n.f2.accept(this, argu);
        return p1Type.equals("Int") && p2Type.equals("Int") ? "Int" : "error";
    }

    public String visit(MinusExpression n, HashMap<String, String> argu) {
        final String p1Type = n.f0.accept(this, argu);
        final String p2Type = n.f2.accept(this, argu);
        return p1Type.equals("Int") && p2Type.equals("Int") ? "Int" : "error";
    }

    public String visit(TimesExpression n, HashMap<String, String> argu) {
        final String p1Type = n.f0.accept(this, argu);
        final String p2Type = n.f2.accept(this, argu);
        return p1Type.equals("Int") && p2Type.equals("Int") ? "Int" : "error";
    }

    public String visit(ArrayLookup n, HashMap<String, String> argu) {
        final String p1Type = n.f0.accept(this, argu);
        final String p2Type = n.f2.accept(this, argu);
        return p1Type.equals("Int[]") && p2Type.equals("Int") ? "Int" : "error";
    }

    public String visit(ArrayLength n, HashMap<String, String> argu) {
        final String type = n.f0.accept(this, argu);
        return type.equals("Int[]") ? "Int" : "error";
    }

    public String visit(MessageSend n, HashMap<String, String> argu) {
        final String classType = n.f0.accept(this, argu);
        final String methodName = n.f2.f0.toString() + "()";
        final String methodType = lookupInheritedSymbol("Global." + classType, methodName, argu);

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

        for (int i = 0; i < parameterList.length; i++) {
            System.out.println(methodType);
            System.out.println(argumentList[i]);
            if (!isSubtypeOf(argumentList[i], parameterList[i], argu)) {
                return "error";
            }
        }

        return returnType;
    }

    public String visit(ExpressionList n, HashMap<String, String> argu) {
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

    public String visit(ExpressionRest n, HashMap<String, String> argu) {
        return n.f1.accept(this, argu);
    }

    public String visit(PrimaryExpression n, HashMap<String, String> argu) {
        return n.f0.accept(this, argu);
    }

    public String visit(IntegerLiteral n, HashMap<String, String> argu) {
        return "Int";
    }

    public String visit(TrueLiteral n, HashMap<String, String> argu) {
        return "Boolean";
    }

    public String visit(FalseLiteral n, HashMap<String, String> argu) {
        return "Boolean";
    }

    public String visit(Identifier n, HashMap<String, String> argu) {
        final String test = n.f0.toString();
        // final String type = lookupSymbol(currentScope + "." + test, argu);
        final String type = lookupSymbol(test, argu);
        if (type != null) {
            return type;
        } else {
            return "error";
        }
    }

    public String visit(ThisExpression n, HashMap<String, String> argu) {
        if (!currentClass.isEmpty()) {
            return currentClass;
        } else {
            return "error";
        }
    }

    public String visit(ArrayAllocationExpression n, HashMap<String, String> argu) {
        final String type = n.f3.accept(this, argu);

        if (type.equals("Int")) {
            return "Int[]";
        } else {
            return "error";
        }
    }

    public String visit(AllocationExpression n, HashMap<String, String> argu) {
        return n.f1.f0.toString();
    }

    public String visit(NotExpression n, HashMap<String, String> argu) {
        final String type = n.f1.accept(this, argu);
        if (type.equals("Boolean")) {
            return type;
        } else {
            return "error";
        }
    }

    public String visit(BracketExpression n, HashMap<String, String> argu) {
        return n.f1.accept(this, argu);
    }

    private String lookupSymbol(String name, HashMap<String, String> symt) {
        String type = lookupSymbolRecursive(currentScope, name, symt);
        if (type == null) {
            type = lookupInheritedSymbol(currentClass, name, symt);
        }

        return type;
    }

    private String lookupSymbolRecursive(String scope, String name, HashMap<String, String> symt) {
        final String key = scope + "." + name;
        final String value = symt.get(key);

        if (value != null) {
            return value;
        }

        final int index = scope.lastIndexOf('.');
        if (index == -1) {
            return null;
        }

        final String newKey = scope.substring(0, index);
        return lookupSymbolRecursive(newKey, name, symt);
    }

    private String lookupInheritedSymbol(String clazz, String name, HashMap<String, String> symt) {
        if (clazz == null) {
            return null;
        }

        final String key = clazz + "." + name;
        final String type = symt.get(key);

        if (type != null) {
            return type;
        }

        final String baseClass = symt.get(clazz);
        return lookupInheritedSymbol(baseClass, name, symt);
    }

    private boolean isSubtypeOf(String derived, String base, HashMap<String, String> symt) {
        if (derived == null) {
            return false;
        }

        if (derived.equals(base)) {
            return true;
        }

        final String baseOfDerived = symt.get(derived);
        return isSubtypeOf(baseOfDerived, base, symt);
    }
}
