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

    for (int i = 1; i <= limit; i = i + 1) {
	Temp = isEven(i);
        if (Temp == 1) {
            sum = sum + i;
        }
    }

    printf("Sum of evens: %d\n", sum);
    return 0;
}