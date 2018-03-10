#define MAKE_INTEGER_TESTS(T, ID, IR, I0, I1) \
static T IR, I0, I1; \
static void test##ID() { \
    IR = I0 + I1; \
    IR = I0 - I1; \
    IR = I0 * I1; \
    IR = I0 / I1; \
    IR = I0 % I1; \
    IR = I0 & I1; \
    IR = I0 | I1; \
    IR = I0 ^ I1; \
    IR = I0 << I1; \
    IR = I0 >> I1; \
} \
\
static void test##ID##Cmp() { \
    IR = I0 < I1; \
    IR = I0 > I1; \
    IR = I0 <= I1; \
    IR = I0 >= I1; \
    IR = I0 == I1; \
    IR = I0 != I1; \
} \
\
static void test##ID##Logical() { \
    IR = I0 && I1; \
    IR = I0 || I1; \
}

#define MAKE_ALL_INTEGER_TESTS(T, ID, VarID) MAKE_INTEGER_TESTS(T, ID, VarID##R, VarID##0, VarID##1)

MAKE_ALL_INTEGER_TESTS(char, Char, C)
MAKE_ALL_INTEGER_TESTS(unsigned char, UChar, UC)
MAKE_ALL_INTEGER_TESTS(short, Short, S)
MAKE_ALL_INTEGER_TESTS(unsigned short, UShort, US)
MAKE_ALL_INTEGER_TESTS(int, Int, I)
MAKE_ALL_INTEGER_TESTS(unsigned int, UInt, UI)
MAKE_ALL_INTEGER_TESTS(long, Long, L)
MAKE_ALL_INTEGER_TESTS(unsigned long, ULong, UL)

#define MAKE_FLOAT_TESTS(T, ID, IR, I0, I1) \
static T IR, I0, I1; \
static void test##ID() { \
    IR = I0 + I1; \
    IR = I0 - I1; \
    IR = I0 * I1; \
    IR = I0 / I1; \
} \
\
static void test##ID##Cmp() { \
    IR = I0 < I1; \
    IR = I0 > I1; \
    IR = I0 <= I1; \
    IR = I0 >= I1; \
    IR = I0 == I1; \
    IR = I0 != I1; \
} \
\
static void test##ID##Logical() { \
    IR = I0 && I1; \
    IR = I0 || I1; \
}

#define MAKE_ALL_FLOAT_TESTS(T, ID, VarID) MAKE_FLOAT_TESTS(T, ID, VarID##R, VarID##0, VarID##1)

MAKE_ALL_FLOAT_TESTS(float, Float, F)
MAKE_ALL_FLOAT_TESTS(double, Double, D)
MAKE_ALL_FLOAT_TESTS(long double, LongDouble, LD)