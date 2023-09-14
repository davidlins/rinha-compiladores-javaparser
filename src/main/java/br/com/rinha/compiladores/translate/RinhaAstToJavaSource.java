package br.com.rinha.compiladores.translate;

import static com.github.javaparser.ast.Modifier.publicModifier;
import static com.github.javaparser.ast.Modifier.staticModifier;
import static com.github.javaparser.ast.NodeList.nodeList;
import static com.github.javaparser.ast.type.PrimitiveType.intType;
import static java.util.Arrays.asList;

import java.io.IOException;
import java.io.InputStream;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BinaryExpr.Operator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.VarType;
import com.github.javaparser.ast.type.VoidType;

import br.com.rinha.compiladores.runtime.InMemoryClass;

public class RinhaAstToJavaSource {

    public static CompilationUnit translate(InputStream in) throws IOException {

        var ast = getAst(in);

        var cu = new CompilationUnit();
        
        var translatedClass = cu.addClass(getClassName(ast));
        translatedClass.addImplementedType(InMemoryClass.class);

        var runCode = new MethodDeclaration(
                nodeList(asList(publicModifier())),
                new VoidType(),
                "runCode");

        translatedClass.addMember(runCode);

        var runCodeBody = runCode.createBody();

        var expression = ast.getJSONObject("expression");

        do {

            if (isFunction(expression)) {
                var function = createMethod(expression);
                translatedClass.addMember(function);

                var functionBody = function.createBody();
                functionBody.addStatement(handleStatement(expression.getJSONObject("value").getJSONObject("value")));

            } else {
                runCodeBody.addStatement(handleStatement(expression));
            }

            expression = expression.getJSONObject("next");

        } while (expression != null);

        return cu;
    }
    
    private static JSONObject getAst(InputStream in) throws IOException {
        
        byte[] targetArray = new byte[in.available()];
        in.read(targetArray);

        return (JSONObject) JSON.parse(targetArray);
    }

    private static String getClassName(JSONObject ast) throws IOException {
       return  ast.getString("name").split("/")[1].replace(".rinha", "");
    }

    public static boolean isFunction(JSONObject expression) {
        var value = expression.getJSONObject("value");
        return (value != null && "Function".equals(value.getString("kind")));
    }

    public static Expression handleFunctionExpression(JSONObject expression) {
        return null;
    }

    public static Statement handleStatement(JSONObject expression) {
        var kind = expression.getString("kind");
        switch (kind) {
        case "If":
            return toIfStmt(expression);
        default:
            return new ExpressionStmt(handleExpression(expression));
        }
    }

    public static Expression handleExpression(JSONObject expression) {
        var kind = expression.getString("kind");
        switch (kind) {
        case "Print":
            return printToExpression(expression);
        case "Str":
            return strToExpression(expression);
        case "Binary":
            return binaryToExpression(expression);
        case "Int":
            return intToExpression(expression);
        case "Let":
            return letToExpression(expression);
        case "Var":
            return varToExpression(expression);
        case "Call":
            return callToExpression(expression);
        default:
            throw new IllegalArgumentException("Unexpected kind value: " + kind);
        }
    }

    public static MethodDeclaration createMethod(JSONObject expression) {
        return new MethodDeclaration(
                nodeList(asList(publicModifier(), staticModifier())),
                expression.getJSONObject("name").getString("text"),
                new PrimitiveType(),
                toTypesParameters(expression.getJSONObject("value").getJSONArray("parameters")));
    }

    public static NodeList<Parameter> toTypesParameters(JSONArray parameters) {

        return new NodeList<>(parameters.stream()
                .map(parameter -> new Parameter(intType(), ((JSONObject) parameter).getString("text")))
                .toList());
    }

    public static Expression intToExpression(JSONObject expression) {
        return new IntegerLiteralExpr(String.valueOf(expression.getInteger("value")));
    }

    public static Expression strToExpression(JSONObject expression) {
        return new StringLiteralExpr(expression.getString("value"));
    }

    public static Expression varToExpression(JSONObject expression) {
        return new NameExpr(expression.getString("text"));
    }

    public static Expression printToExpression(JSONObject expression) {
        return new MethodCallExpr("System.out.print", handleExpression(expression.getJSONObject("value")));
    }

    public static Expression binaryToExpression(JSONObject binary) {

        var left = handleExpression(binary.getJSONObject("lhs"));
        var right = handleExpression(binary.getJSONObject("rhs"));
        var operator = toBinaryOperator(binary.getString("op"));
        return new BinaryExpr(left, right, operator);
    }

    public static Expression letToExpression(JSONObject value) {
        return new VariableDeclarationExpr(
                new VariableDeclarator(new VarType(), new SimpleName(value.getJSONObject("name").getString("text")),
                        handleExpression(value.getJSONObject("value"))));
    }

    private static Statement toIfStmt(JSONObject expresion) {

        var condition = handleExpression(expresion.getJSONObject("condition"));
        var thenStm = toReturnStmt(expresion.getJSONObject("then"));
        var elseStm = toReturnStmt(expresion.getJSONObject("otherwise"));

        return new IfStmt(condition, thenStm, elseStm);
    }

    public static Statement toReturnStmt(JSONObject expresion) {
        return new ReturnStmt(handleExpression(expresion));
    }

    public static Expression callToExpression(JSONObject expression) {
        var callee = expression.getJSONObject("callee").getString("text");
        var arguments = expression.getJSONArray("arguments").stream()
                .map(argument -> handleExpression((JSONObject) argument)).toArray(Expression[]::new);
        return new MethodCallExpr(callee, arguments);
    }

    public static Operator toBinaryOperator(String op) {
        switch (op) {
        case "Add":
            return Operator.PLUS;
        case "Sub":
            return Operator.MINUS;
        case "Mul":
            return Operator.MULTIPLY;
        case "Div":
            return Operator.DIVIDE;
        case "Rem":
            return Operator.REMAINDER;
        case "Eq":
            return Operator.EQUALS;
        case "Neq":
            return Operator.NOT_EQUALS;
        case "Lt":
            return Operator.LESS;
        case "Gt":
            return Operator.GREATER;
        case "Lte":
            return Operator.LESS_EQUALS;
        case "Gte":
            return Operator.GREATER_EQUALS;
        case "And":
            return Operator.AND;
        case "Or":
            return Operator.OR;
        default:
            throw new IllegalArgumentException("Unexpected value: " + op);
        }
    }

}
