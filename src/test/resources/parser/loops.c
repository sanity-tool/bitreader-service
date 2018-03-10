static int I;

extern void foo();

static void testWhile() {
    I = 0;
    while(I < 5) {
        foo();
        I++;
    }
}

static void testDoWhile() {
    I = 0;
    do {
        foo();
        I++;
    } while (I++);
}

static void testFor() {
    for (I = 0; I < 5; I++) {
        foo();
    }
}