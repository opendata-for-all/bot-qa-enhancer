import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import fr.inria.atlanmod.commons.log.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import static java.util.Objects.isNull;

public class Visitor {

    private static String CONFIGURATION_PROPERTIES_FILE = "configuration.properties";

    private static String QA_MODEL_NAME_PARAMETER_TYPE;
    private static String SET_MODEL_ENDPOINT_PARAMETER_TYPE;
    private static String TRANSFORMER_ENDPOINT_PARAMETER_TYPE;
    private static String URL_PARAMETER_TYPE;
    private static String QUESTION_PARAMETER_TYPE;
    private static String DEFAULT_FALLBACK_CORPUS_FILE_PARAMETER_TYPE;
    private static String DEFAULT_FALLBACK_CORPUS_PARAMETER_TYPE;

    private static String QA_MODEL_NAME_PARAMETER_NAME;
    private static String SET_MODEL_ENDPOINT_PARAMETER_NAME;
    private static String TRANSFORMER_ENDPOINT_PARAMETER_NAME;
    private static String URL_PARAMETER_NAME;
    private static String QUESTION_PARAMETER_NAME;
    private static String DEFAULT_FALLBACK_CORPUS_FILE_PARAMETER_NAME;
    private static String DEFAULT_FALLBACK_CORPUS_PARAMETER_NAME;

    private static String QA_MODEL_NAME_PARAMETER_VALUE;
    private static String TRANSFORMER_ENDPOINT_PARAMETER_VALUE;
    private static String SET_MODEL_ENDPOINT_PARAMETER_VALUE;
    private static String URL_PARAMETER_VALUE;
    private static String DEFAULT_FALLBACK_CORPUS_FILE_PARAMETER_VALUE;

    private static String QA_TYPE;
    private static String DEFAULT_FALLBACK_VARIABLE_NAME;

    private static String CHATBOT_NAME;
    private static String CHATBOT_FILE_PATH;

    private static String SET_MODEL_METHOD;
    private static String PROCESS_QA_TEXT_METHOD;
    private static String PROCESS_QA_TABLE_METHOD;
    private static String SET_DEFAULT_FALLBACK_CORPUS_METHOD;
    private static String DEFAULT_FALLBACK_BODY;


    private static void addImports(CompilationUnit cu) {
        cu.addImport(new ImportDeclaration("com.mashape.unirest.http.Unirest", false, false));
        cu.addImport(new ImportDeclaration("fr.inria.atlanmod.commons.log.Log", false, false));
        cu.addImport(new ImportDeclaration("org.apache.commons.io.IOUtils", false, false));
        cu.addImport(new ImportDeclaration("org.json.JSONObject", false, false));
        cu.addImport(new ImportDeclaration("java.io.IOException", false, false));
        cu.addImport(new ImportDeclaration("java.io.InputStream", false, false));
        cu.addImport(new ImportDeclaration("java.util.List", false, false));
        cu.addImport(new ImportDeclaration("java.nio.charset.StandardCharsets", false, false));
        cu.addImport(new ImportDeclaration("java.util.Objects.isNull", true, false));
    }

    private static void setVariables(Properties properties) {

        QA_TYPE = properties.getProperty("QA_TYPE", "table");

        QA_MODEL_NAME_PARAMETER_TYPE = properties.getProperty("QA_MODEL_NAME_PARAMETER_TYPE", "String");
        SET_MODEL_ENDPOINT_PARAMETER_TYPE = properties.getProperty("SET_MODEL_ENDPOINT_PARAMETER_TYPE", "String");
        TRANSFORMER_ENDPOINT_PARAMETER_TYPE = properties.getProperty("TRANSFORMER_ENDPOINT_PARAMETER_TYPE", "String");
        URL_PARAMETER_TYPE = properties.getProperty("URL_PARAMETER_TYPE", "String");
        QUESTION_PARAMETER_TYPE = properties.getProperty("QUESTION_PARAMETER_TYPE", "String");
        DEFAULT_FALLBACK_CORPUS_FILE_PARAMETER_TYPE = properties.getProperty("DEFAULT_FALLBACK_CORPUS_FILE_PARAMETER_TYPE", "String");
        DEFAULT_FALLBACK_CORPUS_PARAMETER_TYPE = properties.getProperty("DEFAULT_FALLBACK_CORPUS_PARAMETER_TYPE", "String");

        QA_MODEL_NAME_PARAMETER_NAME = properties.getProperty("QA_MODEL_NAME_PARAMETER_NAME", "qaModelName");
        SET_MODEL_ENDPOINT_PARAMETER_NAME = properties.getProperty("SET_MODEL_ENDPOINT_PARAMETER_NAME", "setModelEndpoint");
        TRANSFORMER_ENDPOINT_PARAMETER_NAME = properties.getProperty("TRANSFORMER_ENDPOINT_PARAMETER_NAME", "transformerEndpoint");
        URL_PARAMETER_NAME = properties.getProperty("URL_PARAMETER_NAME", "qaModelServerUrl");
        QUESTION_PARAMETER_NAME = properties.getProperty("QUESTION_PARAMETER_NAME", "question");
        DEFAULT_FALLBACK_CORPUS_FILE_PARAMETER_NAME = properties.getProperty("DEFAULT_FALLBACK_CORPUS_FILE_PARAMETER_NAME", "DEFAULT_FALLBACK_CORPUS_FILE");
        DEFAULT_FALLBACK_CORPUS_PARAMETER_NAME = properties.getProperty("DEFAULT_FALLBACK_CORPUS_PARAMETER_NAME", "defaultFallbackCorpus");

        QA_MODEL_NAME_PARAMETER_VALUE = properties.getProperty("QA_MODEL_NAME_PARAMETER_VALUE");
        TRANSFORMER_ENDPOINT_PARAMETER_VALUE = properties.getProperty("TRANSFORMER_ENDPOINT_PARAMETER_VALUE") + "-" + QA_TYPE;
        SET_MODEL_ENDPOINT_PARAMETER_VALUE = properties.getProperty("SET_MODEL_ENDPOINT_PARAMETER_VALUE") + "-" + QA_TYPE;
        URL_PARAMETER_VALUE = properties.getProperty("URL_PARAMETER_VALUE", "http://127.0.0.1:5000/");

        DEFAULT_FALLBACK_CORPUS_FILE_PARAMETER_VALUE = properties.getProperty(
                "DEFAULT_FALLBACK_CORPUS_FILE_PARAMETER_VALUE", "corpus.csv");

        DEFAULT_FALLBACK_VARIABLE_NAME = properties.getProperty("DEFAULT_FALLBACK_VARIABLE_NAME", "defaultFallback");

        CHATBOT_NAME = properties.getProperty("CHATBOT_NAME", "GreetingsBot");
        CHATBOT_FILE_PATH = properties.getProperty("CHATBOT_FILE_PATH") + CHATBOT_NAME + ".java";

        SET_MODEL_METHOD =
                "public static boolean setModel() {\n"
                        + "        JSONObject request = new JSONObject();\n"
                        + "        request.put(\"modelName\", " + QA_MODEL_NAME_PARAMETER_NAME + ");\n"
                        + "        try {\n"
                        + "            JSONObject response = new JSONObject();\n"
                        + "            response = Unirest.post(" + URL_PARAMETER_NAME + " + " + SET_MODEL_ENDPOINT_PARAMETER_NAME + ")\n"
                        + "                    .header(\"Content-Type\", \"application/json\")\n"
                        + "                    .body(request)\n"
                        + "                    .asJson().getBody().getObject();\n"
                        + "            // TODO: check status ok\n"
                        + "        } catch (Exception e) {\n"
                        + "            Log.error(e, \"An error occurred while setting the model, see the attached exception\");\n"
                        + "            defaultFallbackCorpus = null;\n"
                        + "            return false;\n"
                        + "        }\n"
                        + "        return true;\n"
                        + "    }";

        PROCESS_QA_TEXT_METHOD =
                "public static String processQA(" + QUESTION_PARAMETER_TYPE + " " + QUESTION_PARAMETER_NAME + ") {\n"
                        + "        JSONObject request = new JSONObject();\n"
                        + "        request.put(\"question\", " + QUESTION_PARAMETER_NAME + ");\n"
                        + "        request.put(\"corpus\", " + DEFAULT_FALLBACK_CORPUS_PARAMETER_NAME + ");\n"
                        + "        request.put(\"modelName\", " + QA_MODEL_NAME_PARAMETER_NAME + ");\n"
                        + "        JSONObject response = new JSONObject();\n"
                        + "        try {\n"
                        + "            response = Unirest.post(" + URL_PARAMETER_NAME + " + " + TRANSFORMER_ENDPOINT_PARAMETER_NAME + ")\n"
                        + "                    .header(\"Content-Type\", \"application/json\")\n"
                        + "                    .body(request)\n"
                        + "                    .asJson().getBody().getObject();\n"
                        + "            JSONObject modelResponse = response.getJSONObject(" + QA_MODEL_NAME_PARAMETER_NAME + ");\n"
                        + "            // TODO: check if null????\n"
                        + "            String answer = modelResponse.getString(\"answer\");\n"
                        + "            return answer;\n"
                        + "        } catch (Exception e) {\n"
                        + "            Log.error(e, \"An error occurred while computing the answer, see the attached exception\");\n"
                        + "        }\n"
                        + "        return null;\n"
                        + "    }";

        PROCESS_QA_TABLE_METHOD =
                "public static String processQA(" + QUESTION_PARAMETER_TYPE + " " + QUESTION_PARAMETER_NAME + ") {\n"
                        + "        JSONObject request = new JSONObject();\n"
                        + "        request.put(\"question\", " + QUESTION_PARAMETER_NAME + ");\n"
                        + "        request.put(\"corpus\", " + DEFAULT_FALLBACK_CORPUS_PARAMETER_NAME + ");\n"
                        + "        request.put(\"modelName\", " + QA_MODEL_NAME_PARAMETER_NAME + ");\n"
                        + "        JSONObject response = new JSONObject();\n"
                        + "        try {\n"
                        + "            response = Unirest.post(" + URL_PARAMETER_NAME + " + " + TRANSFORMER_ENDPOINT_PARAMETER_NAME + ")\n"
                        + "                    .header(\"Content-Type\", \"application/json\")\n"
                        + "                    .body(request)\n"
                        + "                    .asJson().getBody().getObject();\n"
                        + "            JSONObject modelResponse = response.getJSONObject(" + QA_MODEL_NAME_PARAMETER_NAME + ");\n"
                        + "            // TODO: check if null????\n"
                        + "            List<Object> answer_cells = modelResponse.getJSONArray(\"answer\").toList();\n"
                        + "            String aggregation = modelResponse.getString(\"aggregation\");\n"
                        + "            String answer;\n"
                        + "            switch(aggregation) {\n"
                        + "                case \"NONE\":\n"
                        + "                    try {\n"
                        + "                        answer = (String) answer_cells.get(0);\n"
                        + "                    } catch(IndexOutOfBoundsException iobe) {\n"
                        + "                        Log.error(iobe, \"An error occurred while computing the answer doing a NONE operation, see the attached exception\");\n"
                        + "                        answer = \"NONE: \" + answer_cells;\n"
                        + "                    }"
                        + "                    break;\n"
                        + "                case \"SUM\":\n"
                        + "                    float sum = 0.f;\n"
                        + "                    try {\n"
                        + "                        for (Object cell : answer_cells) {\n"
                        + "                            sum += Float.parseFloat(cell.toString());\n"
                        + "                        }\n"
                        + "                        answer = String.valueOf(sum);\n"
                        + "                    }\n"
                        + "                    catch (NumberFormatException nfe) {\n"
                        + "                        Log.error(nfe, \"An error occurred while computing the answer doing a SUM of terms, see the \"\n"
                        + "                                + \"attached exception\");\n"
                        + "                        answer = \"SUM: \" + answer_cells;\n"
                        + "                    }\n"
                        + "                    break;\n"
                        + "                case \"AVERAGE\":\n"
                        + "                    float average_sum = 0.f;\n"
                        + "                    try {\n"
                        + "                        for (Object cell : answer_cells) {\n"
                        + "                            average_sum += Float.parseFloat(cell.toString());\n"
                        + "                        }\n"
                        + "                        answer = String.valueOf(average_sum/answer_cells.size());\n"
                        + "                    }\n"
                        + "                    catch (NumberFormatException nfe) {\n"
                        + "                        Log.error(nfe, \"An error occurred while computing the answer doing an AVERAGE of terms, see \"\n"
                        + "                                + \"the attached exception\");\n"
                        + "                        answer = \"AVERAGE: \" + answer_cells;\n"
                        + "                    }\n"
                        + "                    break;\n"
                        + "                case \"COUNT\":\n"
                        + "                    answer = String.valueOf(answer_cells.size());\n"
                        + "                    break;\n"
                        + "                default:\n"
                        + "                    answer = answer_cells.toString();\n"
                        + "            }\n"
                        + "            return answer;\n"
                        + "        } catch (Exception e) {\n"
                        + "            Log.error(e, \"An error occurred while computing the answer, see the attached exception\");\n"
                        + "        }\n"
                        + "        return null;"
                        + "    }";

        SET_DEFAULT_FALLBACK_CORPUS_METHOD =
                "private void setDefaultFallbackCorpus() {\n"
                        + "        try (InputStream inputStream =\n"
                        + "                     " + CHATBOT_NAME + ".class.getClassLoader().getResourceAsStream(" + DEFAULT_FALLBACK_CORPUS_FILE_PARAMETER_NAME + ")) {\n"
                        + "            if (isNull(inputStream)) {\n"
                        + "                Log.error(\"Cannot find the file {0}, the default fallback state won't use a language model\",\n"
                        + "                        " + DEFAULT_FALLBACK_CORPUS_FILE_PARAMETER_NAME + ");\n"
                        + "                        defaultFallbackCorpus = null;\n"
                        + "            } else {\n"
                        + "                " + DEFAULT_FALLBACK_CORPUS_PARAMETER_NAME + " = IOUtils.toString(inputStream, StandardCharsets.UTF_8);\n"
                        + "            }\n"
                        + "        } catch (IOException e) {\n"
                        + "            Log.error(\"An error occurred when processing the default fallback corpus file {0}, this process may \" \n"
                        + "                    + \"produce unexpected behavior. Check the logs for more information.\",\n"
                        + "                    " + DEFAULT_FALLBACK_CORPUS_FILE_PARAMETER_NAME + ");\n"
                        + "        }\n"
                        + "        Log.debug(\"Loaded corpus file {0} for the default fallback state\", " + DEFAULT_FALLBACK_CORPUS_FILE_PARAMETER_NAME + ");\n"
                        + "    }";

        DEFAULT_FALLBACK_BODY =
                "fallbackState().body(context -> {\n"
                        + "            if (isNull(defaultFallbackCorpus)) {\n"
                        + "                reactPlatform.reply(context, \"Sorry, I didn't, get it\");\n"
                        + "            } else {\n"
                        + "                " + QUESTION_PARAMETER_TYPE + " " + QUESTION_PARAMETER_NAME + " = context.getIntent().getMatchedInput();\n"
                        + "                String qaAnswer = processQA(question);\n"
                        + "                if (isNull(qaAnswer)) {\n"
                        + "                    reactPlatform.reply(context, \"Sorry, I didn't, get it\");\n"
                        + "                } else {\n"
                        + "                    reactPlatform.reply(context, \"I don't know the exact answer, but I found this potential result:\");\n"
                        + "                    reactPlatform.reply(context, qaAnswer);\n"
                        + "                }\n"
                        + "            }"
                        + "        })";
    }

    private static class defaultFallbackModifier extends ModifierVisitor<Void> {
        @Override
        public MethodDeclaration visit(MethodDeclaration md, Void arg) {
            super.visit(md, arg);
            if (md.getNameAsString().equals("main") && md.getBody().isPresent()) {
                NodeList<Statement> statements = md.getBody().get().getStatements();
                for (Statement statement : statements) {
                    if (statement.isExpressionStmt()) {
                        ExpressionStmt expressionStatement = statement.asExpressionStmt();
                        //System.out.println();
                        if (expressionStatement.getExpression().isVariableDeclarationExpr()) {
                            VariableDeclarationExpr variableDeclarationExpr =
                                    expressionStatement.getExpression().asVariableDeclarationExpr();
                            NodeList<VariableDeclarator> variableDeclarators = variableDeclarationExpr.getVariables();
                            for (VariableDeclarator variableDeclarator : variableDeclarators) {
                                if (variableDeclarator.getNameAsString().equals(DEFAULT_FALLBACK_VARIABLE_NAME)) {
                                    variableDeclarator.setInitializer(DEFAULT_FALLBACK_BODY);
                                }
                            }
                        }
                    }
                }
            }
            return md;
        }
    }

    private static class fieldInserter extends ModifierVisitor<Void> {
        @Override
        public ClassOrInterfaceDeclaration visit(ClassOrInterfaceDeclaration cid, Void arg) {
            super.visit(cid, arg);
            // TODO: check is main bot class ???
            StringLiteralExpr urlExpression = new StringLiteralExpr(URL_PARAMETER_VALUE);
            cid.addFieldWithInitializer(URL_PARAMETER_TYPE, URL_PARAMETER_NAME, urlExpression,
                    Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC);
            StringLiteralExpr setModelEndpointExpression = new StringLiteralExpr(SET_MODEL_ENDPOINT_PARAMETER_VALUE);
            cid.addFieldWithInitializer(SET_MODEL_ENDPOINT_PARAMETER_TYPE, SET_MODEL_ENDPOINT_PARAMETER_NAME, setModelEndpointExpression,
                    Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC);
            StringLiteralExpr transformerEndpointExpression = new StringLiteralExpr(TRANSFORMER_ENDPOINT_PARAMETER_VALUE);
            cid.addFieldWithInitializer(TRANSFORMER_ENDPOINT_PARAMETER_TYPE, TRANSFORMER_ENDPOINT_PARAMETER_NAME, transformerEndpointExpression,
                    Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC);
            StringLiteralExpr qaModelNameExpression = new StringLiteralExpr(QA_MODEL_NAME_PARAMETER_VALUE);
            cid.addFieldWithInitializer(QA_MODEL_NAME_PARAMETER_TYPE, QA_MODEL_NAME_PARAMETER_NAME, qaModelNameExpression,
                    Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC);
            StringLiteralExpr defaultFallbackCorpusFileExpression =
                    new StringLiteralExpr(DEFAULT_FALLBACK_CORPUS_FILE_PARAMETER_VALUE);
            cid.addFieldWithInitializer(DEFAULT_FALLBACK_CORPUS_FILE_PARAMETER_TYPE,
                    DEFAULT_FALLBACK_CORPUS_FILE_PARAMETER_NAME, defaultFallbackCorpusFileExpression,
                    Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC);
            StringLiteralExpr defaultFallbackCorpusExpression = new StringLiteralExpr("");
            cid.addFieldWithInitializer(DEFAULT_FALLBACK_CORPUS_PARAMETER_TYPE, DEFAULT_FALLBACK_CORPUS_PARAMETER_NAME,
                    defaultFallbackCorpusExpression,
                    Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC);

            return cid;
        }
    }

    private static class methodInserter extends ModifierVisitor<Void> {
        @Override
        public ClassOrInterfaceDeclaration visit(ClassOrInterfaceDeclaration cid, Void arg) {
            super.visit(cid, arg);
            // TODO: check is main bot class ???
            MethodDeclaration setModelMethodDeclaration = StaticJavaParser.parseMethodDeclaration(SET_MODEL_METHOD);
            cid
                    .addMethod(setModelMethodDeclaration.getNameAsString(), Modifier.Keyword.PRIVATE,
                            Modifier.Keyword.STATIC)
                    .setType(setModelMethodDeclaration.getType())
                    .setParameters(setModelMethodDeclaration.getParameters())
                    .setBody(setModelMethodDeclaration.getBody().get());

            MethodDeclaration processQAMethodDeclaration;
            if (QA_TYPE.equals("table")) {
                processQAMethodDeclaration = StaticJavaParser.parseMethodDeclaration(PROCESS_QA_TABLE_METHOD);
            }
            else {
                processQAMethodDeclaration = StaticJavaParser.parseMethodDeclaration(PROCESS_QA_TEXT_METHOD);
            }
            cid
                    .addMethod(processQAMethodDeclaration.getNameAsString(), Modifier.Keyword.PRIVATE,
                            Modifier.Keyword.STATIC)
                    .setType(processQAMethodDeclaration.getType())
                    .setParameters(processQAMethodDeclaration.getParameters())
                    .setBody(processQAMethodDeclaration.getBody().get());

            MethodDeclaration setDefaultFallbackCorpusMethodDeclaration =
                    StaticJavaParser.parseMethodDeclaration(SET_DEFAULT_FALLBACK_CORPUS_METHOD);
            cid
                    .addMethod(setDefaultFallbackCorpusMethodDeclaration.getNameAsString(), Modifier.Keyword.PRIVATE,
                            Modifier.Keyword.STATIC)
                    .setType(setDefaultFallbackCorpusMethodDeclaration.getType())
                    .setParameters(setDefaultFallbackCorpusMethodDeclaration.getParameters())
                    .setBody(setDefaultFallbackCorpusMethodDeclaration.getBody().get());
            return cid;
        }
    }

    private static class methodCallInserter extends ModifierVisitor<Void> {
        @Override
        public MethodDeclaration visit(MethodDeclaration md, Void arg) {
            super.visit(md, arg);
            if (md.getNameAsString().equals("main") && md.getBody().isPresent()) {
                NodeList<Statement> statements = md.getBody().get().getStatements();
                MethodDeclaration setDefaultFallbackCorpusMethodDeclaration = StaticJavaParser.parseMethodDeclaration(SET_DEFAULT_FALLBACK_CORPUS_METHOD);
                MethodCallExpr setDefaultFallbackCorpusMethodCallExpr = new MethodCallExpr();
                setDefaultFallbackCorpusMethodCallExpr.setName(setDefaultFallbackCorpusMethodDeclaration.getNameAsString());
                NodeList<Parameter> setDefaultFallbackCorpusParameters =
                        setDefaultFallbackCorpusMethodDeclaration.getParameters();
                for (Parameter parameter : setDefaultFallbackCorpusParameters) {
                    setDefaultFallbackCorpusMethodCallExpr.addArgument(parameter.getNameAsString());
                }
                Statement setDefaultFallbackCorpusMethodCallStatement =
                        new ExpressionStmt(setDefaultFallbackCorpusMethodCallExpr);
                //statements.addFirst(setDefaultFallbackCorpusMethodCallStatement); /////////////////////////////

                MethodDeclaration setModelMethodDeclaration = StaticJavaParser.parseMethodDeclaration(SET_MODEL_METHOD);
                MethodCallExpr setModelMethodCallExpr = new MethodCallExpr();
                setModelMethodCallExpr.setName(setModelMethodDeclaration.getNameAsString());
                NodeList<Parameter> setModelParameters = setModelMethodDeclaration.getParameters();
                for (Parameter parameter : setModelParameters) {
                    setModelMethodCallExpr.addArgument(parameter.getNameAsString());
                }
                Statement setModelMethodCallStatement = new ExpressionStmt(setModelMethodCallExpr);
                //statements.addFirst(setModelMethodCallStatement); ///////////////////////////////////////////////



                Statement ifStatement = new IfStmt(setModelMethodCallExpr,
                        setDefaultFallbackCorpusMethodCallStatement, null);
                statements.addFirst(ifStatement);
            }
            return md;
        }
    }

    private static class moveMainToBottom extends ModifierVisitor<Void> {
        @Override
        public ClassOrInterfaceDeclaration visit(ClassOrInterfaceDeclaration cid, Void arg) {
            super.visit(cid, arg);
            // TODO: check is main bot class ???
            MethodDeclaration mainMethod = cid.getMethodsByName("main").get(0);
            cid.remove(mainMethod);
            cid
                    .addMethod(mainMethod.getNameAsString(), Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC)
                    .setType(mainMethod.getType())
                    .setParameters(mainMethod.getParameters())
                    .setBody(mainMethod.getBody().get());
            return cid;
        }
    }


    public static void main(String[] args) throws Exception {

        Properties properties = new Properties();
        try (InputStream inputStream =
                     Visitor.class.getClassLoader().getResourceAsStream(CONFIGURATION_PROPERTIES_FILE)) {
            if (isNull(inputStream)) {
                Log.error("Cannot find the file {0}", CONFIGURATION_PROPERTIES_FILE);
            } else {
                properties.load(inputStream);
                setVariables(properties);
            }
        } catch (Exception e) {
            Log.error("An error occurred when processing the properties file {0}, this process may produce "
                    + "unexpected behavior. Check the logs for more information.", CONFIGURATION_PROPERTIES_FILE);
        }
        Log.debug("Loaded properties file {0}", CONFIGURATION_PROPERTIES_FILE);

        CompilationUnit cu = StaticJavaParser.parse(new File(CHATBOT_FILE_PATH));

        addImports(cu);


        ModifierVisitor<?> defaultFallbackModifier = new defaultFallbackModifier();
        ModifierVisitor<?> fieldInserter = new fieldInserter();
        ModifierVisitor<?> methodInserter = new methodInserter();
        ModifierVisitor<?> methodCallInserter = new methodCallInserter();
        ModifierVisitor<?> moveMainToBottom = new moveMainToBottom();

        defaultFallbackModifier.visit(cu, null);
        fieldInserter.visit(cu, null);
        methodInserter.visit(cu, null);
        methodCallInserter.visit(cu, null);
        moveMainToBottom.visit(cu, null);

        try {
            FileWriter myWriter = new FileWriter("Enhanced" + CHATBOT_FILE_PATH);
            myWriter.write(cu.toString());
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        System.exit(0);
    }
}
