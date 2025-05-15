import java.util.*;
import java.util.regex.*;

public class Lexer {
    private String input;
    private int position = 0;
    private final List<Token> tokens = new ArrayList<>();

    private static final Set<String> keywords = Set.of(
            "int", "char", "bool", "if", "else", "while", "for", "return", "true", "false", "void", "printf", "scanf"
    );
    private static final Set<String> operators = Set.of(
            "+", "-", "*", "/", "=", "==", "!=", "<", ">", "<=", ">=", "&&", "||", "!"
    );
    private static final Set<String> separators = Set.of(
            "(", ")", "{", "}", ";", ","
    );

    public Lexer(String input) {
        this.input = input;
    }

    public List<Token> tokenize() {
        while (position < input.length()) {
            char current = input.charAt(position);

            if (Character.isWhitespace(current)) {
                position++;
            } else if (Character.isLetter(current) || current == '_') {
                tokenizeIdentifierOrKeyword();
            } else if (Character.isDigit(current)) {
                tokenizeNumber();
            } else if (current == '\'') {
                tokenizeCharLiteral();
            } else if (current == '\"') {
                tokenizeString();
            } else {
                tokenizeOperatorOrSeparator();
            }
        }

        tokens.add(new Token(TokenType.EOF, "EOF"));
        return tokens;
    }
    private void tokenizeOperatorOrSeparator() {

        if (position + 1 < input.length()) {
            String twoChar = input.substring(position, position + 2);
            if (operators.contains(twoChar)) {
                tokens.add(new Token(TokenType.OPERATOR, twoChar));
                position += 2;
                return;
            }
        }


        String oneChar = String.valueOf(input.charAt(position));
        if (operators.contains(oneChar)) {
            tokens.add(new Token(TokenType.OPERATOR, oneChar));
        } else if (separators.contains(oneChar)) {
            tokens.add(new Token(TokenType.SEPARATOR, oneChar));
        } else {
            throw new RuntimeException("Unknown character: " + oneChar);
        }

        position++;
    }

    private void tokenizeString() {
        position++;
        StringBuilder sb = new StringBuilder();
        while (position < input.length() && input.charAt(position) != '\"') {
            sb.append(input.charAt(position));
            position++;
        }
        position++;
        tokens.add(new Token(TokenType.STRING, sb.toString()));
    }

    private void tokenizeCharLiteral() {
        position++;
        if (position < input.length() - 1 && input.charAt(position + 1) == '\'') {
            char value = input.charAt(position);
            position += 2;
            tokens.add(new Token(TokenType.CHAR_LITERAL, String.valueOf(value)));
        } else {
            throw new RuntimeException("Invalid char literal at position " + position);
        }
    }


    private void tokenizeNumber() {
        int start = position;
        while (position < input.length() && Character.isDigit(input.charAt(position))) {
            position++;
        }
        String number = input.substring(start, position);
        tokens.add(new Token(TokenType.NUMBER, number));
    }


    private void tokenizeIdentifierOrKeyword() {
        int start = position;
        while (position < input.length() &&
                (Character.isLetterOrDigit(input.charAt(position)) || input.charAt(position) == '_')) {
            position++;
        }
        String word = input.substring(start, position);
        if (keywords.contains(word)) {
            if (word.equals("true") || word.equals("false"))
                tokens.add(new Token(TokenType.BOOL_LITERAL, word));
            else
                tokens.add(new Token(TokenType.KEYWORD, word));
        } else {
            tokens.add(new Token(TokenType.IDENTIFIER, word));
        }
    }



}