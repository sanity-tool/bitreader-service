static void localAuto() {
    int a = 1;
}

static void localStatic() {
    static int a = 1;
    a = 2;
}

static int ARRAY[10];

static void arrayElementAccess() {
    ARRAY[0] = 1;
    ARRAY[1] = 2;
}

static void arrayDeref() {
    *ARRAY = 1;
}

static int *PTR;

static void ptrElementAccess() {
    PTR[0] = 1;
    PTR[1] = 2;
}

static void ptrDeref() {
    *PTR = 1;
}

struct {
    int field;
} STRUCT, *PTR_STRUCT;

static void structFieldAccess() {
    STRUCT.field = 1;
}

static void structPtrFieldAccess() {
    (*PTR_STRUCT).field = 1;
    PTR_STRUCT->field = 2;
}