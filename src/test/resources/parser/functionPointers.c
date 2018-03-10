void (*FPTR)();

extern void function();

static void takeAddress() {
    FPTR = function;
    FPTR = &function;
}

static void callFromPointer() {
    FPTR();
}