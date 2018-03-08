#include "llvm-c/Core.h"
#include "llvm/ADT/APFloat.h"
#include "llvm/IR/Attributes.h"
#include "llvm/IR/Constants.h"
#include "llvm/IR/DerivedTypes.h"
#include "llvm/IR/DebugInfo.h"
#include "llvm/IR/DiagnosticInfo.h"
#include "llvm/IR/GlobalAlias.h"
#include "llvm/IR/GlobalVariable.h"
#include "llvm/IR/IRBuilder.h"
#include "llvm/IR/InlineAsm.h"
#include "llvm/IR/IntrinsicInst.h"
#include "llvm/IR/LLVMContext.h"
#include "llvm/IR/Module.h"
#include "llvm/Support/Debug.h"
#include "llvm/Support/ErrorHandling.h"
#include "llvm/Support/FileSystem.h"
#include "llvm/Support/ManagedStatic.h"
#include "llvm/Support/MemoryBuffer.h"
#include "llvm/Support/Threading.h"
#include "llvm/Support/raw_ostream.h"

#include <llvm/IR/Instruction.h>
#include <llvm/IR/DebugInfoMetadata.h>

using namespace llvm;

extern "C" {

const char *GetDataArrayString(LLVMValueRef Val) {
    Value *V = unwrap(Val);

    if (ConstantDataSequential *CDS = dyn_cast<ConstantDataSequential>(V)) {
        return CDS->getAsString().data();
    }
    return 0;
}

LLVMRealPredicate GetFCmpPredicate(LLVMValueRef Inst) {
  if (FCmpInst *I = dyn_cast<FCmpInst>(unwrap(Inst)))
    return (LLVMRealPredicate)I->getPredicate();
  if (ConstantExpr *CE = dyn_cast<ConstantExpr>(unwrap(Inst)))
    if (CE->getOpcode() == Instruction::FCmp)
      return (LLVMRealPredicate)CE->getPredicate();
  return (LLVMRealPredicate)0;
}

#define checkSemantics(kind) if (semantics == (const llvm::fltSemantics*)&llvm::APFloat::kind) \
    fprintf(stderr, "semantics not supported: %s\n", #kind);

double GetConstantFPDoubleValue(LLVMValueRef ConstantVal) {
  const APFloat &apf = unwrap<ConstantFP>(ConstantVal)->getValueAPF();
  /*const llvm::fltSemantics *semantics = &apf.getSemantics();
  if (semantics == (const llvm::fltSemantics*)&llvm::APFloat::IEEEdouble) {
    return apf.convertToDouble();
  }
  if (semantics == (const llvm::fltSemantics*)&llvm::APFloat::IEEEsingle) {
    return apf.convertToFloat();
  }

  checkSemantics(IEEEhalf);
  checkSemantics(IEEEquad);
  checkSemantics(PPCDoubleDouble);
  checkSemantics(x87DoubleExtended);*/

  return apf.bitcastToAPInt().bitsToDouble();
}

int SAGetInstructionDebugLocLine(LLVMValueRef instruction) {
    const DebugLoc &loc = unwrap<Instruction>(instruction)->getDebugLoc();
    if (!loc) {
        return -1;
    }
    return loc->getLine();
}

const char *SAGetInstructionDebugLocScopeFile(LLVMValueRef instruction) {
    static std::string result;

    const DebugLoc &loc = unwrap<Instruction>(instruction)->getDebugLoc();
    if (!loc || !loc->getScope()) {
        return 0;
    }

    result = loc->getScope()->getFilename().str();

    return result.c_str();
}

unsigned SAGetDebugMetadataVersionFromModule(LLVMModuleRef module) {
    if (auto *Val = mdconst::dyn_extract_or_null<ConstantInt>(unwrap(module)->getModuleFlag("Debug Info Version"))) {
        return Val->getZExtValue();
    }
    return 0;
}

const char *SAGetMDString(LLVMValueRef value) {
    unsigned len;
    return LLVMGetMDString(value, &len);
}

}
