package ru.urururu.bitreaderservice.cpp;

import ru.urururu.sanity.cpp.llvm.SWIGTYPE_p_LLVMOpaqueType;
import ru.urururu.sanity.cpp.llvm.SWIGTYPE_p_LLVMOpaqueValue;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public interface ParseContext {
    int getGlobalValueId(SWIGTYPE_p_LLVMOpaqueValue value);

    int getTypeId(SWIGTYPE_p_LLVMOpaqueType value);
}
