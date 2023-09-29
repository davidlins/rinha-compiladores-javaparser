package br.com.rinha.compiladores.translate;

import static com.github.javaparser.ast.Modifier.publicModifier;
import static com.github.javaparser.ast.Modifier.staticModifier;
import static com.github.javaparser.ast.NodeList.nodeList;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static org.apache.commons.text.CaseUtils.toCamelCase;

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
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.VarType;
import com.github.javaparser.ast.type.VoidType;

import br.com.rinha.compiladores.runtime.Binary;
import br.com.rinha.compiladores.runtime.InMemoryClass;
import br.com.rinha.compiladores.runtime.Tuple;

public class RinhaAstToJavaSource {

    public static CompilationUnit translate(InputStream in) throws IOException {

        var ast = getAst(in);

        var pathArray = ast.getString("name").replace(".rinha", "").split("/");

        var cu = new CompilationUnit();

        var translatedClass = cu.addClass(getClassName(pathArray));
        translatedClass.addImplementedType(InMemoryClass.class);
        cu.addImport(Tuple.class);
        cu.addImport(Tuple.class.getCanonicalName(), true, true);
        cu.addImport(Binary.class.getCanonicalName(), true, true);
        cu.addImport(InMemoryClass.class.getCanonicalName(), true, true);

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
                populateBlockBody(functionBody, expression.getJSONObject("value").getJSONObject("value"));

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

    private static String getClassName(String[] pathArray) throws IOException {
        return toCamelCase(pathArray[pathArray.length - 1], true, ' ');
    }



    private static MethodDeclaration createMethod(JSONObject expression) {
        return new MethodDeclaration(
                nodeList(asList(publicModifier(), staticModifier())),
                expression.getJSONObject("name").getString("text"),
                new ClassOrInterfaceType(null, "Object"),
                toTypesParameters(expression.getJSONObject("value").getJSONArray("parameters")));
    }

    private static void populateBlockBody(BlockStmt blockStmt, JSONObject expression) {
        do {

            blockStmt.addStatement((!expression.containsKey("next")
                    && !"If".equalsIgnoreCase(expression.getString("kind"))) ? toReturnStmt(expression)
                            : handleStatement(expression));
            expression = expression.getJSONObject("next");

        } while (expression != null);
    }

    public static boolean isFunction(JSONObject expression) {
        var value = expression.getJSONObject("value");
        return (value != null && "Function".equals(value.getString("kind")));
    }

    private static Statement handleStatement(JSONObject expression) {
        var kind = expression.getString("kind");
        switch (kind.toUpperCase()) {
        case "IF":
            return toIfStmt(expression);
        default:
            return new ExpressionStmt(handleExpression(expression));
        }
    }

    private static Expression handleExpression(JSONObject expression) {
        return handleExpression(expression, false);
    }

    private static Expression handleExpression(JSONObject expression, boolean appendToString) {
        var kind = expression.getString("kind");
        switch (kind.toUpperCase()) {
        case "PRINT":
            return printToExpression(expression);
        case "STR":
            return strToExpression(expression);
        case "BOOL":
            boolToExpression(expression);
        case "BINARY":
            return binaryToExpression(expression, appendToString);
        case "INT":
            return intToExpression(expression);
        case "LET":
            return letToExpression(expression);
        case "VAR":
            return varToExpression(expression);
        case "CALL":
            return callToExpression(expression);
        case "TUPLE":
            return tuplaToExpression(expression);
        case "FIRST":
            return callFirstToExpression(expression);
        case "SECOND":
            return callSecondToExpression(expression);
        default:
            throw new IllegalArgumentException("Unexpected kind value: " + kind);
        }
    }

    private static NodeList<Parameter> toTypesParameters(JSONArray parameters) {

        return new NodeList<>(parameters.stream()
                .map(parameter -> new Parameter(new ClassOrInterfaceType(null, "Object"),
                        ((JSONObject) parameter).getString("text")))
                .toList());
    }

    private static Expression intToExpression(JSONObject expression) {
        return new IntegerLiteralExpr(String.valueOf(expression.getLongValue("value")));
    }

    private static Expression strToExpression(JSONObject expression) {
        return new StringLiteralExpr(expression.getString("value"));
    }

    private static Expression boolToExpression(JSONObject expression) {
        return new BooleanLiteralExpr(Boolean.valueOf(expression.getString("value")));
    }

    private static Expression varToExpression(JSONObject expression) {
        var name = expression.getString("text");
        if (name.equals("_")) {
            name = "_"+currentTimeMillis();
        }
        return new NameExpr(name);
    }

    private static Expression printToExpression(JSONObject expression) {
        return new MethodCallExpr("print", handleExpression(expression.getJSONObject("value")));
    }

    private static Expression binaryToExpression(JSONObject binary, boolean appendString) {

        var leftExpression = handleExpression(binary.getJSONObject("lhs"));
        var operator = binary.getString("op");

        if (appendString || operator.equalsIgnoreCase("Add") && leftExpression instanceof StringLiteralExpr) {
            return new BinaryExpr(leftExpression, handleExpression(binary.getJSONObject("rhs"), true),
                    toBinaryOperator(binary.getString("op")));
        } else {
            return new MethodCallExpr("handleBinary", leftExpression, new StringLiteralExpr(operator),
                    handleExpression(binary.getJSONObject("rhs")));
        }
    }

    private static Expression letToExpression(JSONObject value) {

        var name = value.getJSONObject("name").getString("text");
        if (name.equals("_")) {
            name = "underscore";
        }

        return new VariableDeclarationExpr(
                new VariableDeclarator(new VarType(), new SimpleName(name),
                        handleExpression(value.getJSONObject("value"))));
    }

    private static Expression tuplaToExpression(JSONObject value) {
        return new ObjectCreationExpr(null, new ClassOrInterfaceType(null, "Tuple"),
                nodeList(asList(handleExpression(value.getJSONObject("first")),
                        handleExpression(value.getJSONObject("second")))));
    }

    private static Statement toIfStmt(JSONObject expresion) {

        var condition = new CastExpr(new ClassOrInterfaceType(null, "Boolean"),
                handleExpression(expresion.getJSONObject("condition")));

        var thenStm = new BlockStmt();
        populateBlockBody(thenStm, expresion.getJSONObject("then"));

        var elseStm = new BlockStmt();
        populateBlockBody(elseStm, expresion.getJSONObject("otherwise"));

        return new IfStmt(condition, thenStm, elseStm);
    }

    private static Statement toReturnStmt(JSONObject expresion) {
        return new ReturnStmt(handleExpression(expresion));
    }

    private static Expression callToExpression(JSONObject expression) {
        var callee = expression.getJSONObject("callee").getString("text");
       
        if(expression.getJSONArray("arguments").size() > 0) {
            var arguments = expression.getJSONArray("arguments").stream()
                    .map(argument -> {
                        var argumentJsonObject = (JSONObject) argument;
                        if ("Function".equalsIgnoreCase(argumentJsonObject.getString("kind"))) {
                            argumentJsonObject = argumentJsonObject.getJSONObject("value");
                        }
                        return handleExpression(argumentJsonObject);
                    }).toArray(Expression[]::new);
    
            return new MethodCallExpr(callee, arguments);
        }else {
            return handleExpression(expression.getJSONObject("callee"));
        }
    }

    private static Expression callFirstToExpression(JSONObject expression) {
        return new MethodCallExpr("first", handleExpression(expression.getJSONObject("value")));
    }

    private static Expression callSecondToExpression(JSONObject expression) {
        return new MethodCallExpr("second", handleExpression(expression.getJSONObject("value")));
    }

    private static Operator toBinaryOperator(String op) {
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
