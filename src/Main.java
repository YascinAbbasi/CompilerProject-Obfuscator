

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


public class Main {
    public static void main(String[] args) {
        try {
            String input1 = """
                         int main() {
                                                            int x = 3;
                                                           if (x > 0) {
                                                               if (x > 100) {
                                                                   x = 100;
                                                               } else {
                                                                   x = x + 1;
                                                               }
                                                           } else {
                                                               x = -1;
                                                           }
                                                           printf("%d\\n",x);
                                                       }
                    """;
            String input2 = """
                        int add(int a, int b) {
                                   int result = a + b;
                                   return result;
                               }
                    
                               int main() {
                                   int x = 3;
                                   int y = 4;
                                   int sum = add(x, y);
                                   printf("%d\\n",sum);
                               }
                    """;

            String input3 = """
                       int compute(int a) {
                                  if (a == 0) {
                                      return 0;
                                  } else {
                                      return a * 2;
                                  }
                              }
                      int main() {
                                  int i;
                                  int val = 0;
                                  scanf("%d", &val);
                                   i = compute(val); 
                                      printf("%d", i);
                
                                  return 0;
                              }
                    """;
            String input4 = """
                      int average(int a, int b, int c) {
                                                 int sum = a + b + c;
                                                 int avg = sum / 3;
                                                 return avg;
                                             }
                                        
                                             int main() {
                                                 int x = 0;
                      				int y = 0;
                      				int z = 0;
                                                 scanf("%d", &x);
                                                 scanf("%d", &y);
                                                 scanf("%d", &z);
                                         
                                                 int avg = average(x, y, z);
                                         
                                                 if (avg > 50) {
                                                     printf("Passed\\n");
                                                 } else {
                                                     if (avg == 50) {
                                                         printf("Barely passed\\n");
                                                     } else {
                                                         printf("Failed\\n");
                                                     }
                                                 }
                                         
                                                 return 0;
                                             }
                    
                    """;
            String input5 = """
                      int isEven(int n) {
                          while (n >= 2) {
                              n = n - 2;
                          }
                          if (n == 0) {
                              return 1;
                          } else {
                              return 0;
                          }
                      }
                    
                      int main() {
                          int sum = 0;
                          int limit = 0;
                          int Temp = 0;
                          scanf("%d", &limit);
                    
                          for(int i = 1; i <= limit; i = i + 1){
                          Temp = isEven(i);
                              
                              if (Temp == 1) {
                   
                                  sum = sum + i;
                              }
                          }
                    
                          printf("Sum of evens: %d\\n", sum);
                          return 0;
                      }
                    
                    
                    """;




            String inputPath = "D:\\UNI\\TERM6\\Compiler\\input.mc";
            String outputPath = "D:\\UNI\\TERM6\\Compiler\\output.mc";
            String inputCode = new String(Files.readAllBytes(Paths.get(inputPath)));
            PrintWriter writer = new PrintWriter(outputPath);

            Lexer lexer = new Lexer(inputCode);
            List<Token> tokens = lexer.tokenize();
            for (Token token : tokens) {
                System.out.println(token);
            }

            Parser parser = new Parser(tokens, writer);
            parser.parseProgram();
            writer.close();

        } catch (IOException e) {
            System.err.println(" File error: " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println(" Parse error: " + e.getMessage());
        }
    }




}
