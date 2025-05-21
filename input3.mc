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