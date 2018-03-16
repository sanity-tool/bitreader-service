package ru.urururu.bitreaderservice.cpp;

import lombok.AllArgsConstructor;
import ru.urururu.bitreaderservice.dto.SourceRefDto;
import ru.urururu.bitreaderservice.dto.ValueRefDto;
import ru.urururu.sanity.cpp.llvm.SWIGTYPE_p_LLVMOpaqueType;
import ru.urururu.sanity.cpp.llvm.SWIGTYPE_p_LLVMOpaqueValue;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@AllArgsConstructor
public class ParseContextDecorator implements ParseContext {
    private ParseContext ctx;

    @Override
    public ValueRefDto getValueRef(SWIGTYPE_p_LLVMOpaqueValue nativeValue) {
        return ctx.getValueRef(nativeValue);
    }

    @Override
    public int getTypeId(SWIGTYPE_p_LLVMOpaqueType nativeType) {
        return ctx.getTypeId(nativeType);
    }

    @Override
    public Integer getSourceRefId(SourceRefDto sourceRange) {
        return ctx.getSourceRefId(sourceRange);
    }
}
