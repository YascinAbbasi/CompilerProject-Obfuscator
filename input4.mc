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
                               printf("Passed\n");
                           } else {
                               if (avg == 50) {
                                   printf("Barely passed\n");
                               } else {
                                   printf("Failed\n");
                               }
                           }
                    
                           return 0;
                       }