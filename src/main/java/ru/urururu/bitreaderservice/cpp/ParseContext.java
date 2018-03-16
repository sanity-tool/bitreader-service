package ru.urururu.bitreaderservice.cpp;

import ru.urururu.bitreaderservice.dto.SourceRefDto;
import ru.urururu.bitreaderservice.dto.ValueRefDto;
import ru.urururu.sanity.cpp.llvm.SWIGTYPE_p_LLVMOpaqueType;
import ru.urururu.sanity.cpp.llvm.SWIGTYPE_p_LLVMOpaqueValue;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public interface ParseContext {
    ValueRefDto getValueRef(SWIGTYPE_p_LLVMOpaqueValue nativeValue);

    int getTypeId(SWIGTYPE_p_LLVMOpaqueType nativeType);

    Integer getSourceRefId(SourceRefDto sourceRange);
}
