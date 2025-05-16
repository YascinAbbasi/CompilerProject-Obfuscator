import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                    parseVariableDeclaration();

                    return;

                case "if":
                    parseIfStatement();
                    return;

                case "while":
                    parseWhileLoop();
                    return;

            }
        }

        parseAssignment();
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

        match(TokenType.SEPARATOR, true); // ;
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
        while (!peek().value.equals("}")) {
            parseStatement();
        }
        advance();

        System.out.println("if (" + condition + ") { ... }");


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

    private String parsePrimary() {
        Token current = peek();

        if (current.value.equals("(")) {
            advance();
            String expr = parseExpression();
            expectSymbol(")");
            return "(" + expr + ")";
        }

        if (current.type == TokenType.NUMBER || current.type == TokenType.CHAR_LITERAL || current.type == TokenType.BOOL_LITERAL) {
            return advance().value;
        }

        if (current.type == TokenType.IDENTIFIER) {
            return getObfuscatedName(advance().value);
        }

        error("Unexpected token in expression: " + current.value);
        return "";
    }
    private void expectSymbol(String symbol) {
        if (!peek().value.equals(symbol)) {
            error("Expected '" + symbol + "'");
        }
        advance();
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


}
