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
        String code2 = """
            int x = 0;
                    while (x < 5) {
                       x = 2 * (435 - y) / a;
                    }
        """;
        String code3 = """
            for (int i = 0; i < 5; i = i + 1) {
                        x = x + 1;
                    }
        """;
        String code4 = """
           int main(int x, int y) {
                       int sum = x + y;
                       
                  
                      
                       return sum;
      
                   }
        """;
        String code5 = """
           if (x > 0) {
                       int y = 1;
                       if (y < 5) {
                           x = x + 1;
                       } else {
                           while (y < 10) {
                               y = y + 1;
                           }
                       }
                   } else {
                       for (int i = 0; i < 5; i = i + 1) {
                           x = x - 1;
                       }
                   }
                
        """;
        String code6 = """
           int i = 0;
                   while (i < 3) {
                       int j = 0;
                       while (j < 2) {
                        if (x > 0) {
                       int y = 1;
                       if (y < 5) {
                           x = x + 1;
                       } else {
                           while (y < 10) {
                               y = y + 1;
                           }
                       }
                   } else {
                       for (int i = 0; i < 5; i = i + 1) {
                           x = x - 1;
                           main(x,y);
                           asd(i,j,x);
                          
                       }
                   }
                           j = j + 1 - 123;
                       }
                       i = i + 1;
                   }
        
        """;

        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        for (Token token : tokens) {
            System.out.println(token);
        }

        Parser parser = new Parser(tokens);
        parser.parseProgram();

    }
}