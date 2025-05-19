import java.util.*;

public class Parser {
    private List<Token> tokens;
    private int index = 0;

    private Map<String, String> obfuscationMap = new HashMap<>();
    private int varCount = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
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

    private boolean match(TokenType type,boolean advance) {
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


    private String getObfuscatedName(String original) {
        if (!obfuscationMap.containsKey(original)) {
            obfuscationMap.put(original, "a" + varCount++);
        }
        return obfuscationMap.get(original);
    }


    public void parseProgram() {
        while (peek().type != TokenType.EOF) {
            parseStatement();
        }
    }
    private void parseStatement() {
        Token current = peek();

        if (current.type == TokenType.KEYWORD) {
            switch (current.value) {
                case "int":
                case "char":
                case "bool":
                case "void":
                    // بررسی اینکه آیا تعریف تابع است یا متغیر
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

            }
        }
        // سایر موارد
        if (current.type == TokenType.IDENTIFIER) {
            // System.out.println("INSIDEOTHERR??");

            if (index + 1 < tokens.size() && tokens.get(index + 1).value.equals("(")) {
                //  System.out.println("INSIDEFUNCTIONCALL??");
                parseFunctionCall();
            }
            else {
                parseAssignment();
                maybeInsertDeadCode();
            }
        }
        maybeInsertDeadCode();
    }

    private void maybeInsertDeadCode() {
        Random rand = new Random();
        int chance = rand.nextInt(100);

        if (chance < 20) {
            int dummyNum = rand.nextInt(100);
            System.out.println("// dead code");
            System.out.println("int dummyVar" + dummyNum + " = " + dummyNum + " - " + dummyNum + ";");
        }

        if (chance >= 20 && chance < 30) {
            System.out.println("// dead if block");
            System.out.println("if (0) { int dummy = 0; }");
        }
    }

    private void parseReturnStatement() {
        advance();
        String expr = parseExpression();
        match(TokenType.SEPARATOR, true);
        System.out.println("return " + expr + ";");
    }



    private void parseVariableDeclaration() {
        Token type = advance();
        Token name = advance();

        String obfuscatedName = getObfuscatedName(name.value);

        if (peek().type == TokenType.OPERATOR && peek().value.equals("=")) {
            advance();

            String expr = parseExpression();
            System.out.println(type.value + " " + obfuscatedName + " = " + expr + ";");
        } else {
            System.out.println(type.value + " " + obfuscatedName + ";");
        }

        match(TokenType.SEPARATOR, true);
    }



    private void parseAssignment() {
        Token name = advance();
        String obfuscatedName = getObfuscatedName(name.value);

        if (match(TokenType.OPERATOR, false) && peek().value.equals("=")) {
            advance();
            String expr = parseExpression();
            System.out.println(obfuscatedName + " = " + expr + ";");
        } else {
            error("Expected '=' after variable name.");
        }

        match(TokenType.SEPARATOR, true);
    }



    private void parseIfStatement() {
        advance();
        expectSymbol("(");
        String condition = parseCondition();
        expectSymbol(")");

        expectSymbol("{");
        System.out.println("if (" + condition + ") { ... }");
        while (!peek().value.equals("}")) {
            parseStatement();
        }
        advance();

        // System.out.println("if (" + condition + ") { ... }");


        while (peek().type == TokenType.KEYWORD && peek().value.equals("else")) {
            advance();
            if (peek().type == TokenType.KEYWORD && peek().value.equals("if")) {
                advance();
                expectSymbol("(");
                String elifCond = parseCondition();
                expectSymbol(")");
                expectSymbol("{");
                while (!peek().value.equals("}")) {
                    parseStatement();
                }
                advance();
                System.out.println("else if (" + elifCond + ") { ... }");
            } else {
                expectSymbol("{");
                while (!peek().value.equals("}")) {
                    parseStatement();
                }
                advance();
                System.out.println("else { ... }");
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

        String leftObf = getObfuscatedName(left.value);
        System.out.println("while (" + leftObf + " " + op.value + " " + right.value + ") {");


        while (!peek().value.equals("}")) {
            parseStatement();
        }

        advance();
        System.out.println("}");
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


    private String obfuscateIdentifier(String name) {
        Random rand = new Random();
        String obf = getObfuscatedName(name);
        int choice = rand.nextInt(5);

        switch (choice) {
            case 0: return "(" + obf + " + 0)";
            case 1: return "(" + obf + " - 0)";
            case 2: return "(" + obf + " * 1)";
            case 3: return "(" + obf + " / 1)";
            case 4: return "(" + obf + " + (" + obf + " - " + obf + "))";
            default: return obf;
        }
    }

    private String obfuscateNumber(String value) {
        Random rand = new Random();
        int choice = rand.nextInt(5);
        double num = Double.parseDouble(value);

        switch (choice) {
            case 0: return "(" + value + " + 0)";
            case 1: return "(" + (num * 2) + " / 2)";
            case 2: return "(" + (num + 10) + " - 10)";
            case 3: return "(" + (num / 0.5) + " * 0.5)";
            case 4: return "(" + (num * 3) + " / 3)";
            default: return value;
        }
    }

    private String parsePrimary() {
        Token current = peek();

        if (current.value.equals("(")) {
            advance();
            String expr = parseExpression();
            expectSymbol(")");
            return "(" + expr + ")";
        }

        if (current.type == TokenType.NUMBER) {
            String value = advance().value;
            return obfuscateNumber(value);
        }

        if (current.type == TokenType.IDENTIFIER) {
            String name = advance().value;
            return obfuscateIdentifier(name);
        }

        error("Unexpected token in expression: " + current.value);
        return "";
    }





    private void parseForLoop() {
        advance();

        expectSymbol("(");


        parseVariableDeclaration();


        Token left = advance();
        Token op = advance();
        Token right = advance();

        String condition = getObfuscatedName(left.value) + " " + op.value + " " + right.value;
        System.out.println(condition);
        expectSymbol(";");


        Token incLeft = advance();
        advance();
        String rightExpr = parseExpression();

        String increment = getObfuscatedName(incLeft.value) + " = " + rightExpr;
        System.out.println(increment);
        expectSymbol(")");
        expectSymbol("{");


        System.out.println("for (; " + condition + "; " + increment + ") {");


        while (!peek().value.equals("}")) {
            parseStatement();
        }

        advance();

        System.out.println("}");
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
        //String obfuscatedFuncName = getObfuscatedName(funcName.value);
        String obfuscatedFuncName = funcName.value.equals("main") ? "main" : getObfuscatedName(funcName.value);

        expectSymbol("(");


        List<String> obfuscatedParams = new ArrayList<>();
        while (!peek().value.equals(")")) {
            Token paramType = advance();
            Token paramName = advance();
            String obfuscatedParamName = getObfuscatedName(paramName.value);
            obfuscatedParams.add(paramType.value + " " + obfuscatedParamName);

            if (peek().value.equals(",")) {
                advance();
            }
        }

        expectSymbol(")");

        expectSymbol("{");

        System.out.println(returnType.value + " " + obfuscatedFuncName + "(" + String.join(", ", obfuscatedParams) + ") {");


        while (!peek().value.equals("}")) {
            parseStatement();
        }

        advance();
        System.out.println("}");
    }

    private String parseCondition() {
        StringBuilder cond = new StringBuilder();
        while (!peek().value.equals(")") && peek().type != TokenType.EOF) {
            Token t = advance();
            if (t.type == TokenType.IDENTIFIER) {
                cond.append(getObfuscatedName(t.value));
            } else {
                cond.append(t.value);
            }
        }
        return cond.toString();
    }

    private void parseFunctionCall() {
        Token func = advance();
        String obfuscatedName = func.value.equals("main") ? "main" : getObfuscatedName(func.value);

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

        System.out.println(obfuscatedName + "(" + String.join(", ", args) + ");");
    }


}
