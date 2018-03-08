package ru.urururu.bitreaderservice.cpp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.urururu.sanity.api.cfg.NativeCfgBuildingCtx;
import ru.urururu.sanity.api.cfg.ConstCache;
import ru.urururu.sanity.api.cfg.GlobalVariableCache;
import ru.urururu.sanity.api.cfg.RValue;
import ru.urururu.sanity.cpp.llvm.SWIGTYPE_p_LLVMOpaqueBasicBlock;
import ru.urururu.sanity.cpp.llvm.SWIGTYPE_p_LLVMOpaqueType;
import ru.urururu.sanity.cpp.llvm.SWIGTYPE_p_LLVMOpaqueValue;
import ru.urururu.sanity.cpp.llvm.bitreader;

import java.util.function.Function;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class NativeValueParser extends ValueParser<SWIGTYPE_p_LLVMOpaqueType,
        SWIGTYPE_p_LLVMOpaqueValue, SWIGTYPE_p_LLVMOpaqueValue, SWIGTYPE_p_LLVMOpaqueBasicBlock, NativeCfgBuildingCtx> {
    @Autowired
    GlobalVariableCache globals;
    @Autowired
    ConstCache constants;
    @Autowired
    NativeParsersFacade parsers;

    public RValue parseLValue(NativeCfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue value) {
        if (bitreader.LLVMIsAGlobalVariable(value) != null) {
            return globals.get(bitreader.LLVMGetValueName(value), parsers.parse(bitreader.LLVMTypeOf(value)));
        }
        if (bitreader.LLVMIsAArgument(value) != null) {
            return ctx.getParam(value);
        }
        throw new IllegalStateException("Can't parse LValue: " + bitreader.LLVMPrintValueToString(value));
    }

    public RValue parseRValue(NativeCfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue value) {
        if (bitreader.LLVMIsAInstruction(value) != null) {
            return parsers.parseInstructionValue(ctx, value);
        }
        if (bitreader.LLVMIsAConstantExpr(value) != null) {
            return parsers.parseInstructionConst(ctx, value);
        }
        if (bitreader.LLVMIsAConstantInt(value) != null) {
            return constants.get(bitreader.LLVMConstIntGetSExtValue(value), parsers.parse(bitreader.LLVMTypeOf(value)));
        }
        if (bitreader.LLVMIsAConstantFP(value) != null) {
            return constants.get(bitreader.GetConstantFPDoubleValue(value), parsers.parse(bitreader.LLVMTypeOf(value)));
        }
        if (bitreader.LLVMIsAConstantPointerNull(value) != null) {
            return constants.getNull(parsers.parse(bitreader.LLVMTypeOf(value)));
        }
        check(value, bitreader::LLVMIsAConstantStruct, "bitreader::LLVMIsAConstantStruct");
        check(value, bitreader::LLVMIsAConstantAggregateZero, "bitreader::LLVMIsAConstantAggregateZero");
        check(value, bitreader::LLVMIsAConstantArray, "bitreader::LLVMIsAConstantArray");
        check(value, bitreader::LLVMIsAConstantDataArray, "bitreader::LLVMIsAConstantDataArray");
        check(value, bitreader::LLVMIsAConstantDataSequential, "bitreader::LLVMIsAConstantDataSequential");
        check(value, bitreader::LLVMIsAConstantDataVector, "bitreader::LLVMIsAConstantDataVector");
        if (bitreader.LLVMIsAFunction(value) != null) {
            return constants.getFunction(bitreader.LLVMGetValueName(value), parsers.parse(bitreader.LLVMTypeOf(value)));
        }
        return parseLValue(ctx, value);
    }

    private void check(SWIGTYPE_p_LLVMOpaqueValue value, Function<SWIGTYPE_p_LLVMOpaqueValue, SWIGTYPE_p_LLVMOpaqueValue> test, String err) {
        if (test.apply(value) != null) {
            throw new IllegalStateException(err);
        }
    }
}
