int *P, *P2, *P3;

static void testNew() {
    P = new int;
    P2 = new int[5];
    P3 = new int(5);
}

static void testDelete() {
    delete P;
    delete[] P2;
}

struct Foo {
    Foo();
    ~Foo();
} *PSTRUCT, *PSTRUCT2;

static void testNewStruct() {
    PSTRUCT = new Foo();
    PSTRUCT2 = new Foo[5];
}

static void testDeleteStruct() {
    delete PSTRUCT;
    delete[] PSTRUCT2;
}