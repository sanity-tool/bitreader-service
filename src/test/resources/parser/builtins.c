static float FR, F0, F1;
static float DR, D0, D1;
static long double LDR, LD0, LD1;

static void testFloatBuiltinOps() {
  FR = __builtin_fmodf(F0,F1);
  DR = __builtin_fmod(D0,D1);
  LDR = __builtin_fmodl(LD0,LD1);
}
