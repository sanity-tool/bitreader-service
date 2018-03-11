package ru.urururu.bitreaderservice.cpp;

import org.springframework.stereotype.Component;
import ru.urururu.bitreaderservice.dto.TypeDto;
import ru.urururu.sanity.cpp.llvm.*;

import java.util.*;
import java.util.function.BiFunction;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class NativeTypeParser {
    private final Map<LLVMTypeKind, BiFunction<ParseContext, SWIGTYPE_p_LLVMOpaqueType, TypeDto>> parsers = new LinkedHashMap<>();

    public NativeTypeParser() {
        parsers.put(LLVMTypeKind.LLVMVoidTypeKind, this::createPrimitive);
        // half-type?
        parsers.put(LLVMTypeKind.LLVMFloatTypeKind, this::createPrimitive);
        parsers.put(LLVMTypeKind.LLVMDoubleTypeKind, this::createPrimitive);
        parsers.put(LLVMTypeKind.LLVMX86_FP80TypeKind, this::createPrimitive);
        parsers.put(LLVMTypeKind.LLVMIntegerTypeKind, this::createInt);
        parsers.put(LLVMTypeKind.LLVMFunctionTypeKind, this::parseFunction);
        parsers.put(LLVMTypeKind.LLVMStructTypeKind, this::parseStruct);
        parsers.put(LLVMTypeKind.LLVMArrayTypeKind, this::createArray);
        parsers.put(LLVMTypeKind.LLVMPointerTypeKind, this::createPointer);
        parsers.put(LLVMTypeKind.LLVMMetadataTypeKind, this::createPrimitive);
        parsers.put(LLVMTypeKind.LLVMLabelTypeKind, this::createPrimitive);
    }

    private TypeDto createPrimitive(ParseContext ctx, SWIGTYPE_p_LLVMOpaqueType nativeType) {
        return new TypeDto(bitreader.LLVMGetTypeKind(nativeType).toString(), null, null, null);
    }

    private TypeDto createInt(ParseContext ctx, SWIGTYPE_p_LLVMOpaqueType nativeType) {
        return new TypeDto(bitreader.LLVMGetTypeKind(nativeType).toString(), null,
                bitreader.LLVMGetIntTypeWidth(nativeType), null);
    }

    private TypeDto createArray(ParseContext ctx, SWIGTYPE_p_LLVMOpaqueType nativeType) {
        return new TypeDto(bitreader.LLVMGetTypeKind(nativeType).toString(), null, bitreader.LLVMGetArrayLength(nativeType),
                Collections.singletonList(ctx.getTypeId(bitreader.LLVMGetElementType(nativeType))));
    }

    private TypeDto createPointer(ParseContext ctx, SWIGTYPE_p_LLVMOpaqueType nativeType) {
        return new TypeDto(bitreader.LLVMGetTypeKind(nativeType).toString(), null, null,
                Collections.singletonList(ctx.getTypeId(bitreader.LLVMGetElementType(nativeType))));
    }

    public TypeDto parse(ParseContext ctx, SWIGTYPE_p_LLVMOpaqueType type) {
        LLVMTypeKind typeKind = bitreader.LLVMGetTypeKind(type);
        return parsers.getOrDefault(typeKind, this::parseUnknown).apply(ctx, type);
    }

    private TypeDto parseFunction(ParseContext ctx, SWIGTYPE_p_LLVMOpaqueType nativeType) {
        int params = (int) bitreader.LLVMCountParamTypes(nativeType);
        List<Integer> types = new ArrayList<>(1 + params);
        types.add(ctx.getTypeId(bitreader.LLVMGetReturnType(nativeType)));
        if (params != 0) {
            SWIGTYPE_p_p_LLVMOpaqueType paramsBuff = bitreader.calloc_LLVMTypeRef(params, bitreader.sizeof_LLVMTypeRef);
            try {
                bitreader.LLVMGetParamTypes(nativeType, paramsBuff);
                for (int i = 0; i < params; i++) {
                    types.add(ctx.getTypeId(bitreader.getType(paramsBuff, i)));
                }
            } finally {
                bitreader.free_LLVMTypeRef(paramsBuff);
            }
        }

        return new TypeDto(bitreader.LLVMGetTypeKind(nativeType).toString(), null, null,
                types);
    }

    private TypeDto parseStruct(ParseContext ctx, SWIGTYPE_p_LLVMOpaqueType nativeType) {
        int fields = (int) bitreader.LLVMCountStructElementTypes(nativeType);
        List<Integer> types = new ArrayList<>(fields);
        SWIGTYPE_p_p_LLVMOpaqueType fieldsBuff = bitreader.calloc_LLVMTypeRef(fields, bitreaderConstants.sizeof_LLVMTypeRef);
        try {
            bitreader.LLVMGetStructElementTypes(nativeType, fieldsBuff);
            for (int i = 0; i < fields; i++) {
                types.add(ctx.getTypeId(bitreader.getType(fieldsBuff, i)));
            }

            return new TypeDto(bitreader.LLVMGetTypeKind(nativeType).toString(), bitreader.LLVMGetStructName(nativeType),
                    null, types);
        } finally {
            bitreader.free_LLVMTypeRef(fieldsBuff);
        }
    }

    private TypeDto parseUnknown(ParseContext ctx, SWIGTYPE_p_LLVMOpaqueType type) {
        LLVMTypeKind typeKind = bitreader.LLVMGetTypeKind(type);
        throw new IllegalStateException("Can't parse " + typeKind);
    }
}
