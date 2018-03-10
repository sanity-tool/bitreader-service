static int I, *I_P;

void foo();
void bar();

static void testImplicitIf() {
    if (I) {
        foo();
    }
}

static void testImplicitIfElse() {
    if (I) {
        foo();
    } else {
        bar();
    }
}

static void testExplicitIf() {
    if (I != 0) {
        foo();
    }
}
