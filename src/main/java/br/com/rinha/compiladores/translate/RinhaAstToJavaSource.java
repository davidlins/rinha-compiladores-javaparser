package br.com.rinha.compiladores.translate;

import static com.github.javaparser.ast.Modifier.publicModifier;
import static com.github.javaparser.ast.Modifier.staticModifier;
import static com.github.javaparser.ast.Modifier.Keyword.PRIVATE;
import static com.github.javaparser.ast.NodeList.nodeList;
import static com.github.javaparser.ast.type.PrimitiveType.longType;
import static java.util.Arrays.asList;
import static org.apache.commons.text.CaseUtils.toCamelCase;

import java.io.IOException;
import java.io.InputStream;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BinaryExpr.Operator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.VarType;
import com.github.javaparser.ast.type.VoidType;

import br.com.rinha.compiladores.runtime.InMemoryClass;

public class RinhaAstToJavaSource {

    public static CompilationUnit translate(InputStream in) throws IOException {

        var ast = getAst(in);

        var pathArray = ast.getString("name").replace(".rinha", "").split("/");

        var cu = new CompilationUnit();

        var translatedClass = cu.addClass(getClassName(pathArray));
        translatedClass.addImplementedType(InMemoryClass.class);

        translatedClass.addMember(createTupleClass());
        translatedClass.addMember(createFirstMethod());
        translatedClass.addMember(createSecondMethod());
        translatedClass.addMember(createPrintMethod());
        translatedClass.addMember(createPrintIntMethod());

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

    private static ClassOrInterfaceDeclaration createTupleClass() {

        var tupleClass = new ClassOrInterfaceDeclaration(nodeList(asList(publicModifier(), staticModifier())), false,
                "Tuple");

        tupleClass.addField(Object.class, "first", PRIVATE);
        tupleClass.addField(Object.class, "second", PRIVATE);

        var constructorBlockStmt = new BlockStmt();
        constructorBlockStmt.addStatement(new ExpressionStmt(
                new AssignExpr(
                        new FieldAccessExpr(new ThisExpr(), "first"),
                        new NameExpr("first"),
                        AssignExpr.Operator.ASSIGN)));
        constructorBlockStmt.addStatement(new ExpressionStmt(
                new AssignExpr(
                        new FieldAccessExpr(new ThisExpr(), "second"),
                        new NameExpr("second"),
                        AssignExpr.Operator.ASSIGN)));

        var contructor = new ConstructorDeclaration(nodeList(asList(publicModifier())),
                new NodeList<>(),
                new NodeList<>(),
                new SimpleName("Tuple"),
                nodeList(asList(new Parameter(new ClassOrInterfaceType(null, "Object"), "first"),
                        new Parameter(new ClassOrInterfaceType(null, "Object"), "second"))),
                new NodeList<>(), constructorBlockStmt);

        tupleClass.addMember(contructor);

        var getFisrtMethod = new MethodDeclaration(nodeList(asList(publicModifier())),
                new ClassOrInterfaceType(null, "Object"), "getFirst");
        tupleClass.addMember(getFisrtMethod);
        var getFirstBody = getFisrtMethod.createBody();
        getFirstBody.addStatement(new ReturnStmt("first"));

        var getSecondMethod = new MethodDeclaration(nodeList(asList(publicModifier())),
                new ClassOrInterfaceType(null, "Object"), "getSecond");
        tupleClass.addMember(getSecondMethod);
        var getSecondBody = getSecondMethod.createBody();
        getSecondBody.addStatement(new ReturnStmt("second"));

        
        var toStringMethod = new MethodDeclaration(nodeList(asList(publicModifier())),
                new ClassOrInterfaceType(null, "String"), "toString");
        tupleClass.addMember(toStringMethod);
        var toStringBody = toStringMethod.createBody();
        toStringBody.addStatement(new ReturnStmt("\"(\"+first+\", \"+second+\")\""));

        return tupleClass;
    }

    private static MethodDeclaration createFirstMethod() {

        var fisrtMethod = new MethodDeclaration(
                nodeList(asList(publicModifier(), staticModifier())),
                "first",
                new ClassOrInterfaceType(null, "Object"),
                nodeList(asList(new Parameter(new ClassOrInterfaceType(null, "Tuple"), "tuple"))));

        var body = fisrtMethod.getBody().orElseThrow();
        body.addStatement(new ReturnStmt(new MethodCallExpr(new NameExpr("tuple"), new SimpleName("getFirst"))));
        return fisrtMethod;
    }

    private static MethodDeclaration createSecondMethod() {

        var fisrtMethod = new MethodDeclaration(
                nodeList(asList(publicModifier(), staticModifier())),
                "second",
                new ClassOrInterfaceType(null, "Object"),
                nodeList(asList(new Parameter(new ClassOrInterfaceType(null, "Tuple"), "tuple"))));

        var body = fisrtMethod.getBody().orElseThrow();
        body.addStatement(new ReturnStmt(new MethodCallExpr(new NameExpr("tuple"), new SimpleName("getSecond"))));
        return fisrtMethod;
    }
    
    private static MethodDeclaration createPrintMethod() {

        var printMethod = new MethodDeclaration(
                nodeList(asList(publicModifier(), staticModifier())),
                "print",
                new ClassOrInterfaceType(null, "Object"),
                nodeList(asList(new Parameter(new ClassOrInterfaceType(null, "Object"), "value"))));

        var body = printMethod.getBody().orElseThrow();
        body.addStatement(new MethodCallExpr("System.out.print",new NameExpr("value")));
        body.addStatement(new MethodCallExpr("System.out.println"));
        body.addStatement(new ReturnStmt(new NameExpr("value")));
        return printMethod;
    }
    
    private static MethodDeclaration createPrintIntMethod() {

        var printMethod = new MethodDeclaration(
                nodeList(asList(publicModifier(), staticModifier())),
                "print",
                PrimitiveType.intType(),
                nodeList(asList(new Parameter(PrimitiveType.intType(), "value"))));

        var body = printMethod.getBody().orElseThrow();
        body.addStatement(new MethodCallExpr("System.out.print",new NameExpr("value")));
        body.addStatement(new MethodCallExpr("System.out.println"));
        body.addStatement(new ReturnStmt(new NameExpr("value")));
        return printMethod;
    }

    private static MethodDeclaration createMethod(JSONObject expression) {
        return new MethodDeclaration(
                nodeList(asList(publicModifier(), staticModifier())),
                expression.getJSONObject("name").getString("text"),
                PrimitiveType.longType(),
                toTypesParameters(expression.getJSONObject("value").getJSONArray("parameters")));
    }

    private static void populateBlockBody(BlockStmt blockStmt, JSONObject expression) {
        populateBlockBody(blockStmt, expression, false);
    }

    private static void populateBlockBody(BlockStmt blockStmt, JSONObject expression, boolean lastReturn) {
        do {

            blockStmt.addStatement((lastReturn && !expression.containsKey("next")) ? toReturnStmt(expression)
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
        switch (kind) {
        case "If":
            return toIfStmt(expression);
        default:
            return new ExpressionStmt(handleExpression(expression));
        }
    }

    private static Expression handleExpression(JSONObject expression) {
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
        case "Tuple":
            return tuplaToExpression(expression);
        case "First":
            return callFirstToExpression(expression);
        case "Second":
            return callSecondToExpression(expression);
        default:
            throw new IllegalArgumentException("Unexpected kind value: " + kind);
        }
    }

    private static NodeList<Parameter> toTypesParameters(JSONArray parameters) {

        return new NodeList<>(parameters.stream()
                .map(parameter -> new Parameter(longType(), ((JSONObject) parameter).getString("text")))
                .toList());
    }

    private static Expression intToExpression(JSONObject expression) {
        return new LongLiteralExpr(String.valueOf(expression.getLongValue("value")));
    }

    private static Expression strToExpression(JSONObject expression) {
        return new StringLiteralExpr(expression.getString("value"));
    }

    private static Expression varToExpression(JSONObject expression) {
        var name = expression.getString("text");
        if(name.equals("_")) {
            name = "underscore";
        }
        return new NameExpr(name);
    }

    private static Expression printToExpression(JSONObject expression) {
        return new MethodCallExpr("print", handleExpression(expression.getJSONObject("value")));
    }

    private static Expression binaryToExpression(JSONObject binary) {

        var left = handleExpression(binary.getJSONObject("lhs"));
        var right = handleExpression(binary.getJSONObject("rhs"));
        var operator = toBinaryOperator(binary.getString("op"));
        return new BinaryExpr(left, right, operator);
    }

    private static Expression letToExpression(JSONObject value) {
        
        var name = value.getJSONObject("name").getString("text");
        if(name.equals("_")) {
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

        var condition = handleExpression(expresion.getJSONObject("condition"));

        var thenStm = new BlockStmt();
        populateBlockBody(thenStm, expresion.getJSONObject("then"), true);

        var elseStm = new BlockStmt();
        populateBlockBody(elseStm, expresion.getJSONObject("otherwise"), true);

        return new IfStmt(condition, thenStm, elseStm);
    }

    private static Statement toReturnStmt(JSONObject expresion) {
        return new ReturnStmt(handleExpression(expresion));
    }

    private static Expression callToExpression(JSONObject expression) {
        var callee = expression.getJSONObject("callee").getString("text");
        var arguments = expression.getJSONArray("arguments").stream()
                .map(argument -> handleExpression((JSONObject) argument)).toArray(Expression[]::new);
        return new MethodCallExpr(callee, arguments);
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
