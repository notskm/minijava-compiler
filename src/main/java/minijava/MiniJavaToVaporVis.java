package minijava;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import minijava.SymbolTable.ClassBinding;
import minijava.SymbolTable.MethodBinding;
import syntaxtree.*;
import visitor.GJDepthFirst;

public class MiniJavaToVaporVis extends GJDepthFirst<VaporAST, SymbolTable> {
    public Map<String, List<String>> methodTables;
    public List<String> methods = new ArrayList<>();

    private ClassBinding currentClass = null;
    private MethodBinding currentMethod = null;
    private int indentLevel = 0;
    private int tempVariableNumber = 0;
    private int ifLabelNumber = 1;
    private int nullLabelNumber = 1;
    private int whileLabelNumber = 1;
    private int andLabelNumber = 1;
    private int boundsLabelNumber = 1;
    private boolean needsAllocArray = false;

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

    public VaporAST visit(Goal n, SymbolTable symt) {
        VaporAST ret = new VaporAST();

        ret.subprogram = "\n";
        ret.subprogram += methodTablesToVapor() + "\n";

        ret.subprogram += n.f0.accept(this, symt).subprogram;
        for (Node node : n.f1.nodes) {
            ret.subprogram += node.accept(this, symt).subprogram;
        }

        if (needsAllocArray) {
            ret.subprogram += "\n";
            ret.subprogram += "func AllocArray(size)\n";
            beginScope();
            ret.subprogram += indent("bytes = MulS(size 4)\n");
            ret.subprogram += indent("bytes = Add(bytes 4)\n");
            ret.subprogram += indent("v = HeapAllocZ(bytes)\n");
            ret.subprogram += indent("[v] = size\n");
            ret.subprogram += indent("ret v\n");
            endScope();
        }

        return ret;
    }

    public VaporAST visit(MainClass n, SymbolTable symt) {
        currentClass = symt.getClassBinding(n.f1.f0.tokenImage);
        currentMethod = currentClass.getMethod("main");

        beginScope();

        VaporAST varDeclarations = new VaporAST();
        for (Node node : n.f14.nodes) {
            VaporAST var = node.accept(this, symt);
            varDeclarations.subprogram += var.subprogram;
        }

        VaporAST statements = new VaporAST();
        for (Node node : n.f15.nodes) {
            VaporAST statement = node.accept(this, symt);
            statements.subprogram += statement.subprogram;
        }

        VaporAST ret = new VaporAST();
        ret.subprogram += "func Main()\n";
        ret.subprogram += varDeclarations.subprogram;
        ret.subprogram += statements.subprogram;
        ret.subprogram += indent("ret\n");

        endScope();

        currentClass = null;
        return ret;
    }

    public VaporAST visit(TypeDeclaration n, SymbolTable symt) {
        return n.f0.accept(this, symt);
    }

    public VaporAST visit(ClassDeclaration n, SymbolTable symt) {
        currentClass = symt.getClassBinding(n.f1.f0.tokenImage);

        VaporAST varDeclarations = new VaporAST();
        for (Node node : n.f3.nodes) {
            VaporAST var = node.accept(this, symt);
            varDeclarations.subprogram += var.subprogram;
        }

        VaporAST methodDeclarations = new VaporAST();
        for (Node node : n.f4.nodes) {
            VaporAST methodDecl = node.accept(this, symt);
            methodDeclarations.subprogram += methodDecl.subprogram;
        }

        VaporAST ret = new VaporAST();
        ret.subprogram += varDeclarations.subprogram;
        ret.subprogram += methodDeclarations.subprogram;

        currentClass = null;
        return ret;
    }

    public VaporAST visit(ClassExtendsDeclaration n, SymbolTable symt) {
        currentClass = symt.getClassBinding(n.f1.f0.tokenImage);

        VaporAST varDeclarations = new VaporAST();
        for (Node node : n.f5.nodes) {
            VaporAST var = node.accept(this, symt);
            varDeclarations.subprogram += var.subprogram;
        }

        VaporAST methodDeclarations = new VaporAST();
        for (Node node : n.f6.nodes) {
            VaporAST methodDecl = node.accept(this, symt);
            methodDeclarations.subprogram += methodDecl.subprogram;
        }

        VaporAST ret = new VaporAST();
        ret.subprogram += varDeclarations.subprogram;
        ret.subprogram += methodDeclarations.subprogram;

        currentClass = null;
        return ret;
    }

    public VaporAST visit(MethodDeclaration n, SymbolTable symt) {
        final String className = currentClass.getName();
        final String methodName = n.f2.f0.tokenImage;

        currentMethod = currentClass.getMethod(methodName + "()");

        VaporAST args = n.f4.accept(this, symt);
        if (args == null) {
            args = new VaporAST();
        }
        String arguments = "this";

        if (!args.tempExprResult.isEmpty()) {
            arguments += " " + args.tempExprResult;
        }

        beginScope();

        VaporAST varDeclarations = new VaporAST();
        for (Node node : n.f7.nodes) {
            VaporAST var = node.accept(this, symt);
            varDeclarations.subprogram += var.subprogram;
        }
        VaporAST statements = new VaporAST();
        for (Node node : n.f8.nodes) {
            VaporAST statement = node.accept(this, symt);
            statements.subprogram += statement.subprogram;
        }
        VaporAST returnExpr = n.f10.accept(this, symt);

        VaporAST ret = new VaporAST();
        ret.subprogram += "func " + className + "." + methodName + "(" + arguments + ")\n";
        ret.subprogram += varDeclarations.subprogram;
        ret.subprogram += statements.subprogram;
        if (returnExpr.subprogram != "") {
            ret.subprogram += returnExpr.subprogram;
        }
        String tmp = returnExpr.tempExprResult;
        if (returnExpr.exprType == VaporAST.Kind.Deref) {
            tmp = newTempVariable();
            ret.subprogram += indent(tmp + " = " + returnExpr.tempExprResult + "\n");
        }
        ret.subprogram += indent("ret " + tmp + "\n");
        ret.subprogram = "\n" + ret.subprogram;

        endScope();

        return ret;
    }

    public VaporAST visit(FormalParameterList n, SymbolTable symt) {
        VaporAST expr = n.f0.accept(this, symt);

        VaporAST ret = new VaporAST();
        ret.subprogram = expr.subprogram;
        ret.tempExprResult = expr.tempExprResult;

        for (Node node : n.f1.nodes) {
            VaporAST nodeAst = node.accept(this, symt);
            ret.subprogram += nodeAst.subprogram;
            ret.tempExprResult += " " + nodeAst.tempExprResult;
        }

        return ret;
    }

    public VaporAST visit(FormalParameterRest n, SymbolTable symt) {
        return n.f1.accept(this, symt);
    }

    public VaporAST visit(FormalParameter n, SymbolTable symt) {
        return n.f1.accept(this, symt);
    }

    public VaporAST visit(AllocationExpression n, SymbolTable argu) {
        final String className = n.f1.f0.tokenImage;
        final int bytes = argu.getClassBinding(className).getSizeInBytes() + 4;
        final String tmp1 = newTempVariable();

        VaporAST ret = new VaporAST();

        ret.subprogram += indent(tmp1 + " = HeapAllocZ(" + bytes + ")\n");
        ret.subprogram += indent("[" + tmp1 + "] = :vmt_" + className + "\n");

        ret.tempExprResult = tmp1;
        ret.tempExprType = className;
        ret.exprType = VaporAST.Kind.Builtin;

        return ret;

    }

    public VaporAST visit(VarDeclaration n, SymbolTable symt) {
        return n.f1.accept(this, symt);
    }

    public VaporAST visit(AssignmentStatement n, SymbolTable symt) {
        VaporAST lhs = n.f0.accept(this, symt);
        String lhsProg = "";
        if (lhs.subprogram != "") {
            lhsProg = lhs.subprogram;
        }

        VaporAST rhs = n.f2.accept(this, symt);
        String rhsProg = "";
        if (rhs.subprogram != "") {
            rhsProg = rhs.subprogram;
        }
        String rhsVar = rhs.tempExprResult;
        if (rhs.exprType == VaporAST.Kind.None
                || (lhs.exprType == VaporAST.Kind.Deref
                        && (rhs.exprType == VaporAST.Kind.Call || rhs.exprType == VaporAST.Kind.Builtin))) {
            rhsVar = newTempVariable();
            rhsProg += indent(rhsVar + " = " + rhs.tempExprResult + "\n");
        }

        String assign = indent(lhs.tempExprResult + " = " + rhsVar + "\n");

        String errorCheck = "";

        VaporAST ret = new VaporAST();
        ret.subprogram = rhsProg + lhsProg + assign + errorCheck;

        return ret;
    }

    public VaporAST visit(IntegerLiteral n, SymbolTable symt) {
        VaporAST ret = new VaporAST();
        ret.tempExprResult = n.f0.tokenImage;
        ret.tempExprType = "Int";
        ret.exprType = VaporAST.Kind.Trivial;
        return ret;
    }

    public VaporAST visit(TrueLiteral n, SymbolTable symt) {
        VaporAST ret = new VaporAST();
        ret.tempExprResult = "1";
        ret.tempExprType = "Boolean";
        ret.exprType = VaporAST.Kind.Trivial;
        return ret;
    }

    public VaporAST visit(FalseLiteral n, SymbolTable symt) {
        VaporAST ret = new VaporAST();
        ret.tempExprResult = "0";
        ret.tempExprType = "Boolean";
        ret.exprType = VaporAST.Kind.Trivial;
        return ret;
    }

    public VaporAST visit(Identifier n, SymbolTable symt) {
        final boolean isClass = symt.getClassBinding(n.f0.tokenImage) != null;
        final boolean isMethodVar = currentMethod.hasVariable(n.f0.tokenImage);
        final boolean isField = !(isClass || isMethodVar);

        if (!isField) {
            VaporAST ret = new VaporAST();
            ret.tempExprResult = n.f0.tokenImage;
            ret.tempExprType = currentMethod.lookup(ret.tempExprResult);
            ret.exprType = VaporAST.Kind.Trivial;
            return ret;
        } else {
            final int offset = currentClass.getFieldOffset(n.f0.tokenImage);
            VaporAST ret = new VaporAST();
            ret.tempExprResult = "[this" + "+" + offset + "]";
            ret.tempExprType = currentClass.lookup(n.f0.tokenImage);
            ret.exprType = VaporAST.Kind.Deref;
            return ret;
        }
    }

    public VaporAST visit(Statement n, SymbolTable symt) {
        return n.f0.accept(this, symt);
    }

    public VaporAST visit(Block n, SymbolTable symt) {
        VaporAST ret = new VaporAST();
        for (Node node : n.f1.nodes) {
            ret.subprogram += node.accept(this, symt).subprogram;
        }
        return ret;
    }

    public VaporAST visit(BracketExpression n, SymbolTable symt) {
        return n.f1.accept(this, symt);
    }

    public VaporAST visit(IfStatement n, SymbolTable symt) {
        final String elseLabel = "if" + ifLabelNumber + "_else";
        final String endLabel = "if" + ifLabelNumber + "_end";

        ifLabelNumber++;

        VaporAST expr = n.f2.accept(this, symt);

        VaporAST ret = new VaporAST();

        ret.subprogram = expr.subprogram;
        String tmp = expr.tempExprResult;
        if (expr.exprType != VaporAST.Kind.Trivial) {
            tmp = newTempVariable();
            ret.subprogram += indent(tmp + " = " + expr.tempExprResult + "\n");
        }
        ret.subprogram += indent("if0 " + tmp + " goto :" + elseLabel + "\n");

        beginIndent();
        VaporAST trueStatement = n.f4.accept(this, symt);
        ret.subprogram += trueStatement.subprogram;
        ret.subprogram += indent("goto :" + endLabel + "\n");
        endIndent();

        ret.subprogram += indent(elseLabel + ":\n");

        beginIndent();
        VaporAST falseStatement = n.f6.accept(this, symt);
        ret.subprogram += falseStatement.subprogram;
        endIndent();

        ret.subprogram += indent(endLabel + ":\n");

        return ret;
    }

    public VaporAST visit(WhileStatement n, SymbolTable symt) {
        final int labelNum = newWhileLabelNumber();
        final String whileTopLabel = "while" + labelNum + "_top";
        final String whileEndLabel = "while" + labelNum + "_end";

        VaporAST ret = new VaporAST();

        ret.subprogram += indent(whileTopLabel + ":\n");

        VaporAST testExpr = n.f2.accept(this, symt);

        ret.subprogram += testExpr.subprogram;

        String tmp = testExpr.tempExprResult;
        if (testExpr.exprType != VaporAST.Kind.Trivial) {
            tmp = newTempVariable();
            ret.subprogram += indent(tmp + " = " + testExpr.tempExprResult + "\n");
        }

        ret.subprogram += indent("if0 " + tmp + " goto :" + whileEndLabel + "\n");

        beginIndent();
        VaporAST statement = n.f4.accept(this, symt);
        endIndent();

        ret.subprogram += statement.subprogram;
        beginIndent();
        ret.subprogram += indent("goto :" + whileTopLabel + "\n");
        endIndent();

        ret.subprogram += indent(whileEndLabel + ":\n");

        return ret;
    }

    public VaporAST visit(ArrayAllocationExpression n, SymbolTable symt) {
        needsAllocArray = true;

        VaporAST sizeExpression = n.f3.accept(this, symt);

        VaporAST ret = new VaporAST();
        ret.subprogram = sizeExpression.subprogram;

        String tmp = sizeExpression.tempExprResult;
        if (sizeExpression.exprType != VaporAST.Kind.Trivial) {
            tmp = newTempVariable();
            ret.subprogram += indent(tmp + " = " + sizeExpression.tempExprResult);
        }
        ret.tempExprResult += "call :AllocArray(" + tmp + ")";
        ret.exprType = VaporAST.Kind.None;
        return ret;
    }

    public VaporAST visit(ArrayAssignmentStatement n, SymbolTable symt) {
        String boundsLabel = newBoundsLabel();
        String nullLabel = newNullLabel();

        VaporAST identifier = n.f0.accept(this, symt);
        String idProg = "";
        if (idProg != "") {
            idProg = identifier.subprogram;
        }

        String idVar = identifier.tempExprResult;
        if (identifier.exprType != VaporAST.Kind.Trivial) {
            idVar = newTempVariable();
            idProg += indent(idVar + " = " + identifier.tempExprResult + "\n");
        }

        String arrTemp = newTempVariable();

        VaporAST indexExpr = n.f2.accept(this, symt);
        String indexProg = "";
        if (indexExpr.subprogram != "") {
            indexProg = indexExpr.subprogram;
        }

        String indexVar = indexExpr.tempExprResult;
        if (indexExpr.exprType != VaporAST.Kind.Trivial) {
            indexVar = newTempVariable();
            indexProg += indent(indexVar + " = " + indexExpr.tempExprResult + "\n");
        }

        VaporAST rhs = n.f5.accept(this, symt);
        String rhsProg = "";
        if (rhs.subprogram != "") {
            rhsProg = rhs.subprogram;
        }

        String rhsVar = rhs.tempExprResult;
        if (rhs.exprType != VaporAST.Kind.Trivial) {
            rhsVar = newTempVariable();
            rhsProg += indent(rhsVar + " = " + rhs.tempExprResult + "\n");
        }

        String assign = "";
        assign += indent(arrTemp + " = [" + idVar + "]\n");
        assign += indent(arrTemp + " = Lt(" + indexVar + " " + arrTemp + ")\n");
        assign += indent("if " + arrTemp + " goto :" + boundsLabel + "\n");
        beginIndent();
        assign += indent("Error(\"array index out of bounds\")\n");
        endIndent();
        assign += indent(boundsLabel + ":\n");
        assign += indent(arrTemp + " = " + "MulS(" + indexVar + " 4)\n");
        assign += indent(arrTemp + " = " + "Add(" + arrTemp + " " + idVar + ")\n");

        String errorCheck = "";
        errorCheck = indent("if " + idVar + " goto :" + nullLabel +
                "\n");
        beginIndent();
        errorCheck += indent("Error(\"null pointer\")\n");
        endIndent();
        errorCheck += indent(nullLabel + ":\n");

        VaporAST ret = new VaporAST();
        ret.subprogram = idProg;
        ret.subprogram += errorCheck;
        ret.subprogram += indexProg;
        ret.subprogram += assign;
        ret.subprogram += rhsProg;
        ret.subprogram += indent("[" + arrTemp + "+4]" + " = " + rhsVar + "\n");

        return ret;
    }

    public VaporAST visit(ArrayLookup n, SymbolTable symt) {
        VaporAST identifier = n.f0.accept(this, symt);
        String idProg = "";
        if (idProg != "") {
            idProg = identifier.subprogram;
        }

        String idVar = identifier.tempExprResult;
        if (identifier.exprType != VaporAST.Kind.Trivial) {
            idVar = newTempVariable();
            idProg += indent(idVar + " = " + identifier.tempExprResult + "\n");
        }

        VaporAST indexExpr = n.f2.accept(this, symt);
        String indexProg = "";
        if (indexExpr.subprogram != "") {
            indexProg = indexExpr.subprogram;
        }

        String indexVar = indexExpr.tempExprResult;
        if (indexExpr.exprType != VaporAST.Kind.Trivial) {
            indexVar = newTempVariable();
            indexProg += indent(indexVar + " = " + indexExpr.tempExprResult + "\n");
        }

        String assign = "";
        String arrTemp = newTempVariable();
        assign += indent(arrTemp + " = [" + idVar + "]\n");
        assign += indent(arrTemp + " = Lt(" + indexVar + " " + arrTemp + ")\n");
        String boundsLabel = newBoundsLabel();
        assign += indent("if " + arrTemp + " goto :" + boundsLabel + "\n");
        beginIndent();
        assign += indent("Error(\"array index out of bounds\")\n");
        endIndent();
        assign += indent(boundsLabel + ":\n");
        assign += indent(arrTemp + " = " + "MulS(" + indexVar + " 4)\n");
        assign += indent(arrTemp + " = " + "Add(" + arrTemp + " " + idVar + ")\n");

        String errorCheck = "";
        String nullLabel = newNullLabel();
        errorCheck = indent("if " + idVar + " goto :" + nullLabel +
                "\n");
        beginIndent();
        errorCheck += indent("Error(\"null pointer\")\n");
        endIndent();
        errorCheck += indent(nullLabel + ":\n");

        VaporAST ret = new VaporAST();
        ret.subprogram = idProg;
        ret.subprogram += errorCheck;
        ret.subprogram += indexProg;
        ret.subprogram += assign;
        ret.tempExprResult = "[" + arrTemp + "+4]";
        ret.tempExprType = "Int";
        ret.exprType = VaporAST.Kind.Deref;

        return ret;
    }

    public VaporAST visit(Expression n, SymbolTable symt) {
        return n.f0.accept(this, symt);
    }

    public VaporAST visit(PrimaryExpression n, SymbolTable symt) {
        return n.f0.accept(this, symt);
    }

    public VaporAST visit(ThisExpression n, SymbolTable symt) {
        VaporAST ret = new VaporAST();
        ret.tempExprResult = "this";
        ret.tempExprType = currentClass.getName();
        ret.exprType = VaporAST.Kind.Trivial;
        return ret;
    }

    public VaporAST visit(ExpressionList n, SymbolTable symt) {
        VaporAST expr = n.f0.accept(this, symt);

        VaporAST ret = new VaporAST();
        ret.subprogram = expr.subprogram;

        String tmp1 = "";
        if (expr.exprType == VaporAST.Kind.Trivial) {
            tmp1 = expr.tempExprResult;
        } else {
            tmp1 = newTempVariable();
            ret.subprogram += indent(tmp1 + " = " + expr.tempExprResult + "\n");
        }
        ret.tempExprResult = tmp1;

        for (Node node : n.f1.nodes) {
            VaporAST nodeAst = node.accept(this, symt);
            String tmpRest = "";

            if (nodeAst.exprType == VaporAST.Kind.Trivial) {
                tmpRest = nodeAst.tempExprResult;
            } else {
                tmpRest = newTempVariable();
                ret.subprogram += indent(tmpRest + " = " + nodeAst.tempExprResult + "\n");
            }

            ret.subprogram += nodeAst.subprogram;
            ret.tempExprResult += " " + tmpRest;
        }

        return ret;
    }

    public VaporAST visit(ExpressionRest n, SymbolTable symt) {
        return n.f1.accept(this, symt);
    }

    private int findMethodIndex(String type, String method, SymbolTable symt) {
        final List<String> methods = methodTables.get(type);
        final int index = methods.indexOf(method);
        if (index >= 0) {
            return index;
        }
        final ClassBinding clazz = symt.getClassBinding(type);
        final String baseClass = clazz.getBaseClass();
        return findMethodIndex(baseClass, method, symt);
    }

    public VaporAST visit(MessageSend n, SymbolTable symt) {
        VaporAST primaryExpr = n.f0.accept(this, symt);
        String method = n.f2.f0.tokenImage;

        final int methodIndex = findMethodIndex(primaryExpr.tempExprType, method, symt);

        String program = primaryExpr.subprogram;

        String tmp1 = primaryExpr.tempExprResult;
        if (primaryExpr.exprType == VaporAST.Kind.Call) {
            tmp1 = newTempVariable();
            program += indent(tmp1 + " = " + primaryExpr.tempExprResult + "\n");
        }

        String tmp2 = newTempVariable();
        if (primaryExpr.exprType != VaporAST.Kind.Deref) {
            if (!primaryExpr.tempExprResult.equals("this")) {
                final String nullLabel = newNullLabel();
                program += indent("if " + tmp1 + " goto :" + nullLabel + "\n");
                beginIndent();
                program += indent("Error(\"null pointer\")\n");
                endIndent();
                program += indent(nullLabel + ":\n");
            }
        } else {
            program += indent(tmp2 + " = " + tmp1 + "\n");

            final String nullLabel = newNullLabel();
            program += indent("if " + tmp2 + " goto :" + nullLabel + "\n");
            beginIndent();
            program += indent("Error(\"null pointer\")\n");
            endIndent();
            program += indent(nullLabel + ":\n");

            tmp1 = tmp2;
            tmp2 = newTempVariable();
        }

        program += indent(tmp2 + " = [" + tmp1 + "]\n");
        program += indent(tmp2 + " = [" + tmp2 + "+" + methodIndex * 4 + "]\n");

        VaporAST args = n.f4.accept(this, symt);
        if (args == null) {
            args = new VaporAST();
        }
        program += args.subprogram;

        String call = "call " + tmp2 + "(" + tmp1;
        if (args.tempExprResult != "") {
            call += " " + args.tempExprResult;
        }
        call += ")";

        VaporAST ret = new VaporAST();
        ret.subprogram = program;
        ret.tempExprResult = call;
        ret.tempExprType = symt.getClassBinding(primaryExpr.tempExprType).getMethod(method + "()")
                .getReturnType();
        ret.exprType = VaporAST.Kind.Call;
        return ret;
    }

    public VaporAST visit(CompareExpression n, SymbolTable symt) {
        VaporAST lhs = n.f0.accept(this, symt);
        String lhsProg = "";
        String tmp1 = "";
        if (lhs.subprogram != "") {
            lhsProg = lhs.subprogram;
        }

        if (lhs.exprType == VaporAST.Kind.Trivial) {
            tmp1 = lhs.tempExprResult;
        } else {
            tmp1 = newTempVariable();
            lhsProg += indent(tmp1 + " = " + lhs.tempExprResult + "\n");
        }

        VaporAST rhs = n.f2.accept(this, symt);
        String rhsProg = "";
        String tmp2 = "";
        if (rhs.subprogram != "") {
            rhsProg = rhs.subprogram;
        }

        if (rhs.exprType == VaporAST.Kind.Trivial) {
            tmp2 = rhs.tempExprResult;
        } else {
            tmp2 = newTempVariable();
            rhsProg += indent(tmp2 + " = " + rhs.tempExprResult + "\n");
        }

        VaporAST ret = new VaporAST();
        ret.subprogram = lhsProg + rhsProg;
        ret.tempExprResult = "LtS(" + tmp1 + " " + tmp2 + ")";
        ret.tempExprType = "Boolean";
        ret.exprType = VaporAST.Kind.Builtin;

        return ret;
    }

    public VaporAST visit(PlusExpression n, SymbolTable symt) {
        VaporAST lhs = n.f0.accept(this, symt);
        String lhsProg = "";
        String tmp1 = "";
        if (lhs.subprogram != "") {
            lhsProg = lhs.subprogram;
        }

        if (lhs.exprType == VaporAST.Kind.Trivial) {
            tmp1 = lhs.tempExprResult;
        } else {
            tmp1 = newTempVariable();
            lhsProg += indent(tmp1 + " = " + lhs.tempExprResult + "\n");
        }

        VaporAST rhs = n.f2.accept(this, symt);
        String rhsProg = "";
        String tmp2 = "";
        if (rhs.subprogram != "") {
            rhsProg = rhs.subprogram;
        }

        if (rhs.exprType == VaporAST.Kind.Trivial) {
            tmp2 = rhs.tempExprResult;
        } else {
            tmp2 = newTempVariable();
            rhsProg += indent(tmp2 + " = " + rhs.tempExprResult + "\n");
        }

        VaporAST ret = new VaporAST();
        ret.subprogram = lhsProg + rhsProg;
        ret.tempExprResult = "Add(" + tmp1 + " " + tmp2 + ")";
        ret.tempExprType = "Boolean";
        ret.exprType = VaporAST.Kind.Builtin;

        return ret;
    }

    public VaporAST visit(MinusExpression n, SymbolTable symt) {
        VaporAST lhs = n.f0.accept(this, symt);
        String lhsProg = "";
        String tmp1 = "";
        if (lhs.subprogram != "") {
            lhsProg = lhs.subprogram;
        }

        if (lhs.exprType == VaporAST.Kind.Trivial) {
            tmp1 = lhs.tempExprResult;
        } else {
            tmp1 = newTempVariable();
            lhsProg += indent(tmp1 + " = " + lhs.tempExprResult + "\n");
        }

        VaporAST rhs = n.f2.accept(this, symt);
        String rhsProg = "";
        String tmp2 = "";
        if (rhs.subprogram != "") {
            rhsProg = rhs.subprogram;
        }

        if (rhs.exprType == VaporAST.Kind.Trivial) {
            tmp2 = rhs.tempExprResult;
        } else {
            tmp2 = newTempVariable();
            rhsProg += indent(tmp2 + " = " + rhs.tempExprResult + "\n");
        }

        VaporAST ret = new VaporAST();
        ret.subprogram = lhsProg + rhsProg;
        ret.tempExprResult = "Sub(" + tmp1 + " " + tmp2 + ")";
        ret.tempExprType = "Boolean";
        ret.exprType = VaporAST.Kind.Builtin;

        return ret;
    }

    public VaporAST visit(TimesExpression n, SymbolTable symt) {
        VaporAST lhs = n.f0.accept(this, symt);
        String lhsProg = "";
        String tmp1 = "";
        if (lhs.subprogram != "") {
            lhsProg = lhs.subprogram;
        }

        if (lhs.exprType == VaporAST.Kind.Trivial) {
            tmp1 = lhs.tempExprResult;
        } else {
            tmp1 = newTempVariable();
            lhsProg += indent(tmp1 + " = " + lhs.tempExprResult + "\n");
        }

        VaporAST rhs = n.f2.accept(this, symt);
        String rhsProg = "";
        String tmp2 = "";
        if (rhs.subprogram != "") {
            rhsProg = rhs.subprogram;
        }

        if (rhs.exprType == VaporAST.Kind.Trivial) {
            tmp2 = rhs.tempExprResult;
        } else {
            tmp2 = newTempVariable();
            rhsProg += indent(tmp2 + " = " + rhs.tempExprResult + "\n");
        }

        VaporAST ret = new VaporAST();
        ret.subprogram = lhsProg + rhsProg;
        ret.tempExprResult = "MulS(" + tmp1 + " " + tmp2 + ")";
        ret.tempExprType = "Boolean";
        ret.exprType = VaporAST.Kind.Builtin;

        return ret;
    }

    public VaporAST visit(AndExpression n, SymbolTable symt) {
        final int labelNum = newAndLabelNumber();
        final String elseLabel = "ss" + labelNum + "_else";
        final String endLabel = "ss" + labelNum + "_end";

        final String tmp2 = newTempVariable();

        VaporAST ret = new VaporAST();

        VaporAST lhs = n.f0.accept(this, symt);
        String lhsProg = "";
        if (lhs.subprogram != "") {
            lhsProg = lhs.subprogram;
        }
        String tmp1 = lhs.tempExprResult;
        if (lhs.exprType != VaporAST.Kind.Trivial) {
            tmp1 = newTempVariable();
            lhsProg += indent(tmp1 + " = " + lhs.tempExprResult + "\n");
        }

        ret.subprogram += lhsProg;
        ret.subprogram += indent("if0 " + tmp1 + " goto :" + elseLabel + "\n");

        beginIndent();
        VaporAST rhs = n.f2.accept(this, symt);
        String rhsProg = "";
        if (rhs.subprogram != "") {
            rhsProg = rhs.subprogram;
        }
        rhsProg += indent(tmp2 + " = " + rhs.tempExprResult + "\n");
        endIndent();

        ret.subprogram += rhsProg;
        beginIndent();
        ret.subprogram += indent("goto :" + endLabel + "\n");
        endIndent();

        ret.subprogram += indent(elseLabel + ":\n");

        beginIndent();
        ret.subprogram += indent(tmp2 + " = 0\n");
        endIndent();

        ret.subprogram += indent(endLabel + ":\n");
        ret.tempExprResult = tmp2;
        ret.tempExprType = "Boolean";
        ret.exprType = VaporAST.Kind.Trivial;

        return ret;
    }

    public VaporAST visit(PrintStatement n, SymbolTable symt) {
        VaporAST expr = n.f2.accept(this, symt);
        VaporAST ret = new VaporAST();

        ret.subprogram = expr.subprogram;

        String tmp = expr.tempExprResult;
        if (expr.exprType != VaporAST.Kind.Trivial) {
            tmp = newTempVariable();
            ret.subprogram += indent(tmp + " = " + expr.tempExprResult + "\n");
        }

        ret.subprogram += indent("PrintIntS(" + tmp + ")\n");

        return ret;
    }

    public VaporAST visit(NotExpression n, SymbolTable symt) {
        VaporAST ret = new VaporAST();

        VaporAST expr = n.f1.accept(this, symt);

        ret.subprogram = expr.subprogram;

        String tmp = expr.tempExprResult;
        if (expr.exprType != VaporAST.Kind.Trivial) {
            tmp = newTempVariable();
            ret.subprogram += indent(tmp + " = " + expr.tempExprResult + "\n");
        }

        ret.tempExprResult = "Sub(1 " + tmp + ")";
        ret.tempExprType = "Boolean";
        ret.exprType = VaporAST.Kind.Builtin;

        return ret;
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

    private int newWhileLabelNumber() {
        int ret = whileLabelNumber;
        whileLabelNumber++;
        return ret;
    }

    // private int newIfLabelNumber() {
    // int ret = ifLabelNumber;
    // ifLabelNumber++;
    // return ret;
    // }

    private int newAndLabelNumber() {
        int ret = andLabelNumber;
        andLabelNumber++;
        return ret;
    }

    private String newNullLabel() {
        int ret = nullLabelNumber;
        nullLabelNumber++;
        return "null" + ret;
    }

    private String newBoundsLabel() {
        int num = boundsLabelNumber;
        boundsLabelNumber++;
        return "bounds" + num;
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
        endIndent();
    }
}
