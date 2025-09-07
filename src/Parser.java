  import java.io.PrintWriter;
import java.util.*;

public class Parser {
    private List<Token> tokens;
    private int index = 0;
    private PrintWriter writer;


    private Map<String, String> deobfMap = new HashMap<>();
    private int varCount = 0;


    private Map<String, String> funcMap = new HashMap<>();
    private int funcCount = 1;


    private final List<String> simpleNames = Arrays.asList(
            "x","y","z","a","b","c","d","m","n","j","i","k"
    );
    private int simpleIndex = 0;

    public Parser(List<Token> tokens, PrintWriter writer) {
        this.tokens = tokens;
        this.writer = writer;
    }

    private Token peek() {
        if (index < tokens.size()) {
            return tokens.get(index);
        }
        return new Token(TokenType.EOF, "");
    }

    private Token advance() {
        if (index < tokens.size()) {
            return tokens.get(index++);
        }
        return new Token(TokenType.EOF, "");
    }

    private boolean match(TokenType type, boolean advance) {
        if (peek().type == type) {
            if (advance) {
                advance();
            }
            return true;
        }
        return false;
    }

    private void error(String msg) {
        throw new RuntimeException("Parse Error: " + msg);
    }

    private String getDeobfuscatedName(String original) {

        if (simpleNames.contains(original)) {
            return original;
        }
        if (deobfMap.containsKey(original)) {
            return deobfMap.get(original);
        }
        if (simpleIndex < simpleNames.size()) {
            String name = simpleNames.get(simpleIndex++);
            deobfMap.put(original, name);
            return name;
        }

        String fallback = "var" + (varCount++);
        deobfMap.put(original, fallback);
        return fallback;
    }

    private String getDeobfuscatedFunctionName(String original) {
        if ("main".equals(original)) return "main";
        if (funcMap.containsKey(original)) return funcMap.get(original);
        String name = "Function" + (funcCount++);
        funcMap.put(original, name);
        return name;
    }

    public void parseProgram() {
        while (peek().type != TokenType.EOF) {
            parseStatement();
        }
    }

    private void parseStatement() {
        Token current = peek();


        if (current.type == TokenType.KEYWORD && current.value.equals("int")) {

            if (index + 1 < tokens.size() && tokens.get(index + 1).type == TokenType.IDENTIFIER) {
                String nextName = tokens.get(index + 1).value;
                if (nextName.startsWith("deadVar")) {

                    advance();
                    advance();

                    while (peek().type != TokenType.EOF && !peek().value.equals(";")) {
                        advance();
                    }
                    if (peek().value.equals(";")) advance();
                    return;
                }
            }
        }

        if (current.type == TokenType.KEYWORD) {
            switch (current.value) {
                case "int":
                case "char":
                case "bool":
                case "void":
                    if (index + 2 < tokens.size() && tokens.get(index + 2).value.equals("(")) {
                        parseFunctionDeclaration();
                    } else {
                        parseVariableDeclaration();
                    }
                    return;

                case "if":
                    parseIfStatement();
                    return;

                case "while":
                    parseWhileLoop();
                    return;

                case "for":
                    parseForLoop();
                    return;
                case "return":
                    parseReturnStatement();
                    return;
                case "printf":
                    parsePrintf();
                    return;
                case "scanf":
                    parseScanf();
                    return;
            }
        }

        if (current.type == TokenType.IDENTIFIER) {

            if (current.value.startsWith("deadVar")) {

                advance();

                if (!peek().value.equals("(")) {

                    while (peek().type != TokenType.EOF && !peek().value.equals(";")) {
                        advance();
                    }
                    if (peek().value.equals(";")) advance();
                    return;
                } else {

                    while (peek().type != TokenType.EOF && !peek().value.equals(";")) {
                        advance();
                    }
                    if (peek().value.equals(";")) advance();
                    return;
                }
            }

            if (index + 1 < tokens.size() && tokens.get(index + 1).value.equals("(")) {
                parseFunctionCall();
            } else {
                parseAssignment();

            }
            return;
        }


        if (peek().value.equals(";")) {
            advance();
            return;
        }


        advance();
    }


    private void maybeInsertDeadCode() { /* noop */ }

    private void parseReturnStatement() {
        advance();
        String expr = parseExpression();
        match(TokenType.SEPARATOR, true);
        System.out.println("return " + expr + ";");
        writer.println("return " + expr + ";");
    }

    private void parseVariableDeclaration() {
        Token type = advance();
        Token name = advance();


        if (name.value.startsWith("deadVar")) {

            while (peek().type != TokenType.EOF && !peek().value.equals(";")) {
                advance();
            }
            if (peek().value.equals(";")) advance();
            return;
        }

        String deobfName = getDeobfuscatedName(name.value);

        if (peek().type == TokenType.OPERATOR && peek().value.equals("=")) {
            advance();
            String expr = parseExpression();
            System.out.println(type.value + " " + deobfName + " = " + expr + ";");
            writer.println(type.value + " " + deobfName + " = " + expr + ";");
        } else {
            System.out.println(type.value + " " + deobfName + ";");
            writer.println(type.value + " " + deobfName + ";");
        }

        match(TokenType.SEPARATOR, true);
    }

    private void parseAssignment() {
        Token name = advance();


        if (name.value.startsWith("deadVar")) {
            while (peek().type != TokenType.EOF && !peek().value.equals(";")) {
                advance();
            }
            if (peek().value.equals(";")) advance();
            return;
        }

        String deobfName = getDeobfuscatedName(name.value);

        if (match(TokenType.OPERATOR, false) && peek().value.equals("=")) {
            advance();
            String expr = parseExpression();
            System.out.println(deobfName + " = " + expr + ";");
            writer.println(deobfName + " = " + expr + ";");
        } else {
            error("Expected '=' after variable name.");
        }

        match(TokenType.SEPARATOR, true);
    }

    private void parseIfStatement() {
        advance();
        expectSymbol("(");


        if (peek().type == TokenType.NUMBER && (index + 1 < tokens.size()) && tokens.get(index + 1).value.equals(")")) {
            String num = advance().value;
            expectSymbol(")");


            if (peek().value.equals("{")) {
                skipBlock();
            } else {

                skipSingleStatement();
            }


            while (peek().type == TokenType.KEYWORD && peek().value.equals("else")) {

                advance();
                if (peek().type == TokenType.KEYWORD && peek().value.equals("if")) {

                    parseIfStatement();
                } else {

                    expectSymbol("{");
                    System.out.println("else {");
                    writer.println("else {");
                    while (!peek().value.equals("}")) {
                        parseStatement();
                    }
                    advance();
                    System.out.println("}");
                    writer.println("}");
                    break;
                }
            }
            return;
        }


        String condition = parseCondition();
        expectSymbol(")");

        expectSymbol("{");
        System.out.println("if (" + condition + ") {");
        writer.println("if (" + condition + ") {");

        while (!peek().value.equals("}")) {
            parseStatement();
        }
        advance(); 
        System.out.println("}");
        writer.println("}");

      
        while (peek().type == TokenType.KEYWORD && peek().value.equals("else")) {
            advance(); 
            if (peek().type == TokenType.KEYWORD && peek().value.equals("if")) {
                advance();
                expectSymbol("(");
                String elifCond = parseCondition();
                expectSymbol(")");
                expectSymbol("{");

                System.out.println("else if (" + elifCond + ") {");
                writer.println("else if (" + elifCond + ") {");

                while (!peek().value.equals("}")) {
                    parseStatement();
                }
                advance();
                System.out.println("}");
                writer.println("}");
            } else {
                expectSymbol("{");
                System.out.println("else {");
                writer.println("else {");

                while (!peek().value.equals("}")) {
                    parseStatement();
                }
                advance();
                System.out.println("}");
                writer.println("}");
                break;
            }
        }
    }

    private void parseWhileLoop() {
        advance();

        if (!match(TokenType.SEPARATOR, false) || !peek().value.equals("(")) {
            error("Expected '(' after 'while'");
        }
        advance();

        Token left = advance();
        Token op = advance();
        Token right = advance();

        if (!match(TokenType.SEPARATOR, false) || !peek().value.equals(")")) {
            error("Expected ')' after condition");
        }
        advance();

        if (!match(TokenType.SEPARATOR, false) || !peek().value.equals("{")) {
            error("Expected '{' to start while body");
        }
        advance();

        String leftName = getDeobfuscatedName(left.value);
        String rightValue = (right.type == TokenType.IDENTIFIER) ? getDeobfuscatedName(right.value) : right.value;

        System.out.println("while (" + leftName + " " + op.value + " " + rightValue + ") {");
        writer.println("while (" + leftName + " " + op.value + " " + rightValue + ") {");

        while (!peek().value.equals("}")) {
            parseStatement();
        }

        advance(); 
        System.out.println("}");
        writer.println("}");
    }

    private String parseExpression() {
        return parseTerm();
    }

    private String parseTerm() {
        String expr = parseFactor();

        while (peek().type == TokenType.OPERATOR && (peek().value.equals("+") || peek().value.equals("-"))) {
            String op = advance().value;
            String right = parseFactor();
            expr = "(" + expr + " " + op + " " + right + ")";
        }

        return expr;
    }

    private String parseFactor() {
        String expr = parsePrimary();

        while (peek().type == TokenType.OPERATOR && (peek().value.equals("*") || peek().value.equals("/"))) {
            String op = advance().value;
            String right = parsePrimary();
            expr = "(" + expr + " " + op + " " + right + ")";
        }

        return expr;
    }

    private String parsePrimary() {
        Token current = peek();

        if (current.value.equals("(")) {
            advance();
            String expr = parseExpression();
            expectSymbol(")");
            return "(" + expr + ")";
        }

        if (current.value.equals("-")) {
            if (index + 1 < tokens.size() && tokens.get(index + 1).type == TokenType.NUMBER) {
                advance();
                String num = advance().value;
                return "-" + num; 
            }
        }

        if (current.type == TokenType.NUMBER) {
            String value = advance().value;
            return value; 
        }

        if (current.type == TokenType.IDENTIFIER) {
            if (index + 1 < tokens.size() && tokens.get(index + 1).value.equals("(")) {
                return parseFunctionCallInline();
            } else {
                String name = advance().value;
               
                if (name.startsWith("deadVar")) {
                  
                    return "0";
                }
                return getDeobfuscatedName(name);
            }
        }

        error("Unexpected token in expression: " + current.value);
        return "";
    }

    private String parseVariableDeclarationAsString() {
        Token type = advance();
        Token name = advance();

        
        if (name.value.startsWith("deadVar")) {
            while (peek().type != TokenType.EOF && !peek().value.equals(";")) advance();
            if (peek().value.equals(";")) advance();
            return type.value + " " + "/*removed*/";
        }

        String deobfName = getDeobfuscatedName(name.value);

        StringBuilder result = new StringBuilder();
        result.append(type.value).append(" ").append(deobfName);

        if (peek().type == TokenType.OPERATOR && peek().value.equals("=")) {
            advance();
            String expr = parseExpression();
            result.append(" = ").append(expr);
        }

        match(TokenType.SEPARATOR, true);
        return result.toString();
    }

    private void parseForLoop() {
        advance(); 
        expectSymbol("(");

      
        if (index + 1 < tokens.size() &&
                tokens.get(index).type == TokenType.KEYWORD &&
                tokens.get(index).value.equals("int") &&
                tokens.get(index + 1).value.equals("i")) {

            
            while (peek().type != TokenType.EOF && !peek().value.equals("{")) {
                advance();
            }
            if (peek().value.equals("{")) {
                skipBlock(); 
            }
            return;
        }

       
        String initialization = parseVariableDeclarationAsString();

        Token left = advance();
        Token op = advance();
        Token right = advance();

        String rightValue = (right.type == TokenType.IDENTIFIER) ? getDeobfuscatedName(right.value) : right.value;
        String condition = getDeobfuscatedName(left.value) + " " + op.value + " " + rightValue;
        expectSymbol(";");

        Token incLeft = advance();
        advance(); 
        String rightExpr = parseExpression();
        String increment = getDeobfuscatedName(incLeft.value) + " = " + rightExpr;
        expectSymbol(")");
        expectSymbol("{");

        System.out.println("for (" + initialization + "; " + condition + "; " + increment + ") {");
        writer.println("for (" + initialization + "; " + condition + "; " + increment + ") {");

        while (!peek().value.equals("}")) {
            parseStatement();
        }
        advance();

        System.out.println("}");
        writer.println("}");
    }

    private void expectSymbol(String symbol) {
        if (!peek().value.equals(symbol)) {
            error(" AAAA Expected '" + symbol + "'");
        }
        advance();
    }

    private void parseFunctionDeclaration() {
        Token returnType = advance();
        Token funcName = advance();
        String deobfFuncName = funcName.value.equals("main") ? "main" : getDeobfuscatedFunctionName(funcName.value);

        expectSymbol("(");

        List<String> deobfParams = new ArrayList<>();
        while (!peek().value.equals(")")) {
            Token paramType = advance();
            Token paramName = advance();
            String deobfParamName = getDeobfuscatedName(paramName.value);
            deobfParams.add(paramType.value + " " + deobfParamName);

            if (peek().value.equals(",")) {
                advance();
            }
        }

        expectSymbol(")");

        expectSymbol("{");

        System.out.println(returnType.value + " " + deobfFuncName + "(" + String.join(", ", deobfParams) + ") {");
        writer.println(returnType.value + " " + deobfFuncName + "(" + String.join(", ", deobfParams) + ") {");

        while (!peek().value.equals("}")) {
            parseStatement();
        }

        advance();
        System.out.println("}");
        writer.println("}");
    }

    private String parseCondition() {
        StringBuilder cond = new StringBuilder();
        while (!peek().value.equals(")") && peek().type != TokenType.EOF) {
            Token t = advance();
            if (t.type == TokenType.IDENTIFIER) {
                if (t.value.startsWith("deadVar")) {
                    cond.append("0"); 
                } else {
                    cond.append(getDeobfuscatedName(t.value));
                }
            } else {
                cond.append(t.value);
            }
        }
        return cond.toString();
    }

    private void parsePrintf() {
        advance();
        expectSymbol("(");

        Token format = advance();
        StringBuilder args = new StringBuilder("\"" + format.value.replace("\"", "") + "\"");

        if (peek().value.equals(",")) {
            advance();

            while (!peek().value.equals(")")) {
                Token var = advance();
                if (var.type == TokenType.IDENTIFIER) {
                    
                    if (!var.value.startsWith("deadVar")) {
                        args.append(", ").append(getDeobfuscatedName(var.value));
                    }
                } else {
                    args.append(", ").append(var.value);
                }

                if (peek().value.equals(",")) {
                    advance();
                }
            }
        }

        expectSymbol(")");
        match(TokenType.SEPARATOR, true);

        System.out.println("printf(" + args + ");");
        writer.println("printf(" + args + ");");
    }

    private void parseScanf() {
        advance();
        expectSymbol("(");

        Token format = advance();
        StringBuilder args = new StringBuilder("\"" + format.value.replace("\"", "") + "\"");

        while (peek().value.equals(",")) {
            advance();
            Token amp = advance();
            if (!amp.value.equals("&")) {
                error("Expected '&' before variable in scanf");
            }
            Token var = advance();
           
            if (!var.value.startsWith("deadVar")) {
                String deobfVar = getDeobfuscatedName(var.value);
                args.append(", &").append(deobfVar);
            }
        }

        expectSymbol(")");
        match(TokenType.SEPARATOR, true);

        System.out.println("scanf(" + args + ");");
        writer.println("scanf(" + args + ");");
    }

    private void parseFunctionCall() {
        Token func = advance();
        String deobfName = func.value.equals("main") ? "main" : getDeobfuscatedFunctionName(func.value);

        expectSymbol("(");

        List<String> args = new ArrayList<>();
        while (!peek().value.equals(")")) {
            args.add(parseExpression());

            if (peek().value.equals(",")) {
                advance();
            }
        }

        expectSymbol(")");
        match(TokenType.SEPARATOR, true);

        System.out.println(deobfName + "(" + String.join(", ", args) + ");");
        writer.println(deobfName + "(" + String.join(", ", args) + ");");
    }

    private String parseFunctionCallInline() {
        Token func = advance();
        String deobfName = func.value.equals("main") ? "main" : getDeobfuscatedFunctionName(func.value);

        expectSymbol("(");

        List<String> args = new ArrayList<>();
        while (!peek().value.equals(")")) {
            args.add(parseExpression());

            if (peek().value.equals(",")) {
                advance();
            }
        }

        expectSymbol(")");
        return deobfName + "(" + String.join(", ", args) + ")";
    }

    
    private void skipBlock() {
        if (!peek().value.equals("{")) return;
        
        advance();
        int depth = 1;
        while (index < tokens.size() && depth > 0) {
            Token t = advance();
            if (t.value.equals("{")) depth++;
            else if (t.value.equals("}")) depth--;
        }
       
    }

    
    private void skipSingleStatement() {
        if (peek().value.equals("{")) {
            skipBlock();
            return;
        }
       
        while (index < tokens.size() && !peek().value.equals(";")) {
            advance();
        }
        if (peek().value.equals(";")) advance();
    }
}
