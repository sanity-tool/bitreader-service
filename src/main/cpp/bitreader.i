%module bitreader

%ignore LLVMConstGEP2;
%ignore LLVMConstInBoundsGEP2;

%apply (char *STRING, size_t LENGTH) { (const char data[], size_t len) }

%{
#include "llvm-c/Core.h"
#include "llvm-c/Support.h"
#include "llvm-c/IRReader.h"
#include <stdio.h>

int SAGetInstructionDebugLocLine(LLVMValueRef instruction);
unsigned SAGetDebugMetadataVersionFromModule(LLVMModuleRef module);

typedef struct {
        char*         buff;
        int           len;
} workit_t;

LLVMModuleRef parse(const char data[], size_t len) {
    LLVMModuleRef m;
    LLVMMemoryBufferRef membuf;
    char *errmsg;
    LLVMContextRef ctx = LLVMGetGlobalContext();

    int argc = 2;
    const char *argv[] = { "dummy", "-strip-debug-info=0" };

    LLVMParseCommandLineOptions(argc, argv, "llvm .bc reader library");

    if (!(membuf = LLVMCreateMemoryBufferWithMemoryRange(data, len, "membuf", 0))) {
        fprintf(stderr, "LLVMCreateMemoryBufferWithMemoryRange returned 0\n");
        return 0;
    }
    if (LLVMParseIRInContext(ctx, membuf, &m, &errmsg)) {
        fprintf(stderr, "LLVMParseIRInContext: %s\n", errmsg);
        return 0;
    }
    return m;
}

const char *getMDString(LLVMValueRef valueRef) {
    unsigned int len;
    return LLVMGetMDString(valueRef, &len);
}

LLVMTypeRef getType(LLVMTypeRef *types, int i) {
    return types[i];
}

LLVMValueRef getValue(LLVMValueRef *values, int i) {
    return values[i];
}

const char *GetDataArrayString(LLVMValueRef Val);
LLVMRealPredicate GetFCmpPredicate(LLVMValueRef Inst);
double GetConstantFPDoubleValue(LLVMValueRef ConstantVal);

const char *SAGetInstructionDebugLocScopeFile(LLVMValueRef instruction);

const char *SAGetMDString(LLVMValueRef value);

%}

%contract LLVMGetOperand (LLVMValueRef Val, int Index) {
require:
   Index >= 0 && Index < LLVMGetNumOperands(Val);
}

%typemap(javacode) SWIGTYPE * %{
  public boolean equals(Object obj) {
    boolean equal = false;
    if (obj instanceof $javaclassname)
      equal = ((($javaclassname)obj).swigCPtr == this.swigCPtr);
    return equal;
  }

  public int hashCode() {
     return Long.hashCode(swigCPtr);
  }
%}

%include "llvm-c/Types.h"
%include "llvm-c/Support.h"
%include "llvm-c/Core.h"
%include "llvm-c/IRReader.h"

%include "cmalloc.i"
%allocators(LLVMTypeRef);
%allocators(LLVMValueRef);

LLVMTypeRef getType(LLVMTypeRef *types, int i);
LLVMValueRef getValue(LLVMValueRef *values, int i);

LLVMModuleRef parse(const char data[], size_t len);
const char *getMDString(LLVMValueRef valueRef);

const char* GetDataArrayString(LLVMValueRef Val);
LLVMRealPredicate GetFCmpPredicate(LLVMValueRef Inst);
double GetConstantFPDoubleValue(LLVMValueRef ConstantVal);

unsigned SAGetDebugMetadataVersionFromModule(LLVMModuleRef module);

int SAGetInstructionDebugLocLine(LLVMValueRef instruction);

%javamethodmodifiers SAGetInstructionDebugLocScopeFile "public synchronized";
const char *SAGetInstructionDebugLocScopeFile(LLVMValueRef instruction);

const char *SAGetMDString(LLVMValueRef value);

%pragma(java) jniclasscode=%{
  static {
     System.loadLibrary("irreader");
  }
%}
