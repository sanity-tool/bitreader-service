int *I_P, I;

static void addressOf() {
    I_P = &I;
}

static void deref() {
    *I_P = I;
}