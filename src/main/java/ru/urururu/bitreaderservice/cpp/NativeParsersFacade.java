package ru.urururu.bitreaderservice.cpp;

import org.springframework.stereotype.Component;
import ru.urururu.sanity.api.ParsersFacade;
import ru.urururu.sanity.api.cfg.NativeCfgBuildingCtx;
import ru.urururu.sanity.cpp.llvm.SWIGTYPE_p_LLVMOpaqueBasicBlock;
import ru.urururu.sanity.cpp.llvm.SWIGTYPE_p_LLVMOpaqueType;
import ru.urururu.sanity.cpp.llvm.SWIGTYPE_p_LLVMOpaqueValue;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class NativeParsersFacade extends ParsersFacade<SWIGTYPE_p_LLVMOpaqueType,
        SWIGTYPE_p_LLVMOpaqueValue, SWIGTYPE_p_LLVMOpaqueValue, SWIGTYPE_p_LLVMOpaqueBasicBlock, NativeCfgBuildingCtx> {
}
