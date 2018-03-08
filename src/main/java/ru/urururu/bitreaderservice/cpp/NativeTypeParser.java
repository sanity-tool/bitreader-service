package ru.urururu.bitreaderservice.cpp;

import org.springframework.stereotype.Component;
import ru.urururu.sanity.api.TypeParser;
import ru.urururu.sanity.api.cfg.Type;
import ru.urururu.sanity.cpp.llvm.*;
import ru.urururu.util.FinalMap;
import ru.urururu.util.Iterables;

import java.util.*;
import java.util.function.Function;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class NativeTypeParser extends TypeParser<SWIGTYPE_p_LLVMOpaqueType> implements ParserListener {
    private final Map<LLVMTypeKind, Function<SWIGTYPE_p_LLVMOpaqueType, Type>> parsers = FinalMap.createHashMap();

    public NativeTypeParser() {
        parsers.put(LLVMTypeKind.LLVMVoidTypeKind, t -> createVoid());
        // half-type?
        parsers.put(LLVMTypeKind.LLVMFloatTypeKind, t -> createFloat());
        parsers.put(LLVMTypeKind.LLVMDoubleTypeKind, t -> createDouble());
        parsers.put(LLVMTypeKind.LLVMX86_FP80TypeKind, t -> createLongDouble());
        parsers.put(LLVMTypeKind.LLVMIntegerTypeKind, t -> createInt(bitreader.LLVMGetIntTypeWidth(t)));
        parsers.put(LLVMTypeKind.LLVMFunctionTypeKind, this::parseFunction);
        parsers.put(LLVMTypeKind.LLVMStructTypeKind, this::parseStruct);
        parsers.put(LLVMTypeKind.LLVMArrayTypeKind,
                t -> createArray(bitreader.LLVMGetElementType(t), bitreader.LLVMGetArrayLength(t)));
        parsers.put(LLVMTypeKind.LLVMPointerTypeKind, t -> createPointer(bitreader.LLVMGetElementType(t)));
        parsers.put(LLVMTypeKind.LLVMMetadataTypeKind, t -> createMetadata());
    }

    public Type parse(SWIGTYPE_p_LLVMOpaqueType type) {
        LLVMTypeKind typeKind = bitreader.LLVMGetTypeKind(type);
        return typesCache.computeIfAbsent(type, key -> parsers.getOrDefault(typeKind, this::parseUnknown).apply(type));
    }

    private Type parseFunction(SWIGTYPE_p_LLVMOpaqueType type) {
        int params = (int) bitreader.LLVMCountParamTypes(type);
        if (params != 0) {
            SWIGTYPE_p_p_LLVMOpaqueType paramsBuff = bitreader.calloc_LLVMTypeRef(params, bitreader.sizeof_LLVMTypeRef);
            try {
                bitreader.LLVMGetParamTypes(type, paramsBuff);
                return createFunction(bitreader.LLVMGetReturnType(type),
                        Iterables.indexed(i -> bitreader.getType(paramsBuff, i), () -> params));
            } finally {
                bitreader.free_LLVMTypeRef(paramsBuff);
            }
        }

        return createFunction(bitreader.LLVMGetReturnType(type), Collections.emptyList());
    }

    private Type parseStruct(SWIGTYPE_p_LLVMOpaqueType type) {
        int fields = (int) bitreader.LLVMCountStructElementTypes(type);
        SWIGTYPE_p_p_LLVMOpaqueType fieldsBuff = bitreader.calloc_LLVMTypeRef(fields, bitreaderConstants.sizeof_LLVMTypeRef);
        try {
            bitreader.LLVMGetStructElementTypes(type, fieldsBuff);
            return createStruct(type, bitreader.LLVMGetStructName(type), Iterables.indexed(i -> bitreader.getType(fieldsBuff, i), () -> fields));
        } finally {
            bitreader.free_LLVMTypeRef(fieldsBuff);
        }
    }

    private Type parseUnknown(SWIGTYPE_p_LLVMOpaqueType type) {
        LLVMTypeKind typeKind = bitreader.LLVMGetTypeKind(type);
        throw new IllegalStateException("Can't parse " + typeKind);
    }

    @Override
    public void onModuleFinished(SWIGTYPE_p_LLVMOpaqueModule module) {
        structCache.clear();
        typesCache.clear();
    }
}
