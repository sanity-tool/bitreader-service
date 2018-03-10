static int I;

static void testSwitchWithDefault() {
    switch (I) {
        case 1:
            foo1();
            break;
        case 2:
            foo2();
            break;
        default:
            fooDefault();
    }
    fooAfter();
}

static void testSwitchNoDefault() {
    switch (I) {
        case 1:
            foo1();
            break;
        case 2:
            foo2();
            break;
    }
    fooAfter();
}

static void testSwitchFallthrough() {
    switch (I) {
        case 1:
            foo1();
        case 2:
            foo2();
    }
    fooAfter();
}