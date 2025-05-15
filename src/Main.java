import java.util.List;

public class Main {
    public static void main(String[] args) {

        String code = """
            if (x > 10) {
                        y = 5;
                    } else {
                        y = 6;
                    }
        """;

        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        for (Token token : tokens) {
            System.out.println(token);
        }

    }
}