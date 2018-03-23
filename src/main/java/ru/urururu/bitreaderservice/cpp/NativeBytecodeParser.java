package ru.urururu.bitreaderservice.cpp;

import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.urururu.bitreaderservice.dto.*;
import ru.urururu.sanity.cpp.llvm.*;

import java.util.*;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class NativeBytecodeParser {
    @Autowired
    private List<ParserListener> parserListeners;
    @Autowired
    private NativeTypeParser typeParser;
    @Autowired
    private NativeSourceRangeFactory sourceRangeFactory;

    public ModuleDto parse(byte[] bitcode) {
        SWIGTYPE_p_LLVMOpaqueModule m = bitreader.parse(bitcode);

        if (m == null) {
            return null;
        }

        try {
            parserListeners.forEach(l -> l.onModuleStarted(m));
            return toModule(m);
        } finally {
            parserListeners.forEach(l -> l.onModuleFinished(m));
            bitreader.LLVMDisposeModule(m);
        }
    }

    private ModuleDto toModule(SWIGTYPE_p_LLVMOpaqueModule nativeModule) {
        ModuleParseContext ctx = new ModuleParseContext();

        Map<ValueRefDto, FunctionDto> functions = new LinkedHashMap<>();

        SWIGTYPE_p_LLVMOpaqueValue nativeFunction = bitreader.LLVMGetFirstFunction(nativeModule);
        while (nativeFunction != null) {
            try {
                if (bitreader.LLVMGetFirstBasicBlock(nativeFunction) != null) {
                    functions.put(ctx.getValueRef(nativeFunction), toFunction(ctx, nativeFunction));
                } else {
                    // todo store some info?
                }
            } catch (Exception e) {
                throw new IllegalStateException("Can't parse function: " + bitreader.LLVMGetValueName(nativeFunction), e);
            }

            nativeFunction = bitreader.LLVMGetNextFunction(nativeFunction);
        }

        return new ModuleDto(functions.values(), ctx.getTypes(), ctx.globalValues, ctx.sourceRefs.keySet());
    }

    private FunctionDto toFunction(ParseContext ctx, SWIGTYPE_p_LLVMOpaqueValue nativeFunction) {
        List<SWIGTYPE_p_LLVMOpaqueValue> nativeParams = new ArrayList<>();
        List<ValueDto> params = new ArrayList<>();
        SWIGTYPE_p_LLVMOpaqueValue nativeParam = bitreader.LLVMGetFirstParam(nativeFunction);
        while (nativeParam != null) {
            nativeParams.add(nativeParam);
            params.add(toValue(ctx, nativeParam).build());
            nativeParam = bitreader.LLVMGetNextParam(nativeParam);
        }

        List<SWIGTYPE_p_LLVMOpaqueValue> nativeBlocks = new ArrayList<>();
        Map<SWIGTYPE_p_LLVMOpaqueBasicBlock, BlockDto> blocks = new LinkedHashMap<>();

        SWIGTYPE_p_LLVMOpaqueBasicBlock nativeBlock = bitreader.LLVMGetFirstBasicBlock(nativeFunction);
        while (nativeBlock != null) {
            nativeBlocks.add(bitreader.LLVMBasicBlockAsValue(nativeBlock));
            nativeBlock = bitreader.LLVMGetNextBasicBlock(nativeBlock);
        }

        ParseContext functionCtx = new ParseContextDecorator(ctx) {
            @Override
            public ValueRefDto getValueRef(SWIGTYPE_p_LLVMOpaqueValue nativeValue) {
                LLVMValueKind kind = bitreader.LLVMGetValueKind(nativeValue);
                if (kind == LLVMValueKind.LLVMArgumentValueKind) {
                    return new ValueRefDto(ValueRefDto.ValueRefKind.Argument, Preconditions.checkElementIndex(nativeParams.indexOf(nativeValue), nativeParams.size()));
                } else if (kind == LLVMValueKind.LLVMBasicBlockValueKind) {
                    return new ValueRefDto(ValueRefDto.ValueRefKind.Block, Preconditions.checkElementIndex(nativeBlocks.indexOf(nativeValue), nativeBlocks.size()));
                } else {
                    return super.getValueRef(nativeValue);
                }
            }
        };

        nativeBlock = bitreader.LLVMGetFirstBasicBlock(nativeFunction);
        while (nativeBlock != null) {
            blocks.put(nativeBlock, toBlock(functionCtx, nativeBlock));

            nativeBlock = bitreader.LLVMGetNextBasicBlock(nativeBlock);
        }

        BlockDto entryBlock = blocks.get(bitreader.LLVMGetEntryBasicBlock(nativeFunction));

        return new FunctionDto(ctx.getValueRef(nativeFunction), blocks.values(), instanceIndexOf(blocks.values(), entryBlock), params);
    }

    private BlockDto toBlock(ParseContext ctx, SWIGTYPE_p_LLVMOpaqueBasicBlock nativeBlock) {
        Map<SWIGTYPE_p_LLVMOpaqueValue, InstructionDto> instructions = new LinkedHashMap<>();
        List<SWIGTYPE_p_LLVMOpaqueValue> nativeInstructions = new ArrayList<>();

        ParseContext blockCtx = new ParseContextDecorator(ctx) {
            @Override
            public ValueRefDto getValueRef(SWIGTYPE_p_LLVMOpaqueValue nativeValue) {
                LLVMValueKind kind = bitreader.LLVMGetValueKind(nativeValue);

                if (kind == LLVMValueKind.LLVMInstructionValueKind) {
                    return new ValueRefDto(ValueRefDto.ValueRefKind.Instruction, nativeInstructions.indexOf(nativeValue));
                }

                return super.getValueRef(nativeValue);
            }
        };

        SWIGTYPE_p_LLVMOpaqueValue nativeInstruction = bitreader.LLVMGetFirstInstruction(nativeBlock);
        while (nativeInstruction != null) {
            nativeInstructions.add(nativeInstruction);
            instructions.put(nativeInstruction, toInstruction(blockCtx, nativeInstruction));

            nativeInstruction = bitreader.LLVMGetNextInstruction(nativeInstruction);
        }
        return new BlockDto(instructions.values());
    }

    private InstructionDto toInstruction(ParseContext ctx, SWIGTYPE_p_LLVMOpaqueValue nativeInstruction) {
        LLVMOpcode opcode = bitreader.LLVMGetInstructionOpcode(nativeInstruction);
        String predicate = null;
        if (opcode == LLVMOpcode.LLVMFCmp) {
            predicate = bitreader.GetFCmpPredicate(nativeInstruction).toString();
        } else if (opcode == LLVMOpcode.LLVMICmp) {
            predicate = bitreader.LLVMGetICmpPredicate(nativeInstruction).toString();
        }

        return new InstructionDto(
                opcode.toString(),
                ctx.getTypeId(bitreader.LLVMTypeOf(nativeInstruction)),
                getOperands(ctx, nativeInstruction),
                ctx.getSourceRefId(sourceRangeFactory.getSourceRange(nativeInstruction)),
                predicate
        );
    }

    private List<ValueRefDto> getOperands(ParseContext ctx, SWIGTYPE_p_LLVMOpaqueValue nativeValue) {
        int numOperands = bitreader.LLVMGetNumOperands(nativeValue);
        List<ValueRefDto> operands = new ArrayList<>();
        for (int i = 0; i < numOperands; i++) {
            operands.add(ctx.getValueRef(bitreader.LLVMGetOperand(nativeValue, i)));
        }
        return operands;
    }

    private ValueDto.ValueDtoBuilder toValue(ParseContext ctx, SWIGTYPE_p_LLVMOpaqueValue nativeValue) {
        return ValueDto.builder()
                .kind(bitreader.LLVMGetValueKind(nativeValue).toString())
                .name(bitreader.LLVMGetValueName(nativeValue))
                .typeId(ctx.getTypeId(bitreader.LLVMTypeOf(nativeValue)))
                ;
    }

    private <E> int instanceIndexOf(Collection<E> collection, E item) {
        int i = 0;
        for (E e : collection) {
            if (e == item) {
                return i;
            }
            i++;
        }

        return -1;
    }

    class ModuleParseContext implements ParseContext {
        List<ValueDto> globalValues = new ArrayList<>();
        Map<SWIGTYPE_p_LLVMOpaqueValue, ValueRefDto> globalValueIds = new HashMap<>();
        Map<Integer, TypeDto> types = new TreeMap<>();
        Map<SWIGTYPE_p_LLVMOpaqueType, Integer> typeIds = new HashMap<>();
        Map<SourceRefDto, Integer> sourceRefs = new LinkedHashMap<>();

        @Override
        public Integer getSourceRefId(SourceRefDto sourceRange) {
            if (sourceRange == null) {
                return null;
            }

            return sourceRefs.computeIfAbsent(sourceRange, sr -> sourceRefs.size());
        }

        @Override
        public ValueRefDto getValueRef(SWIGTYPE_p_LLVMOpaqueValue nativeValue) {
            ValueRefDto valueRef = globalValueIds.get(nativeValue);

            if (valueRef != null) {
                return valueRef;
            }

            final ValueDto value;

            LLVMValueKind kind = bitreader.LLVMGetValueKind(nativeValue);
            if (kind == LLVMValueKind.LLVMFunctionValueKind || kind == LLVMValueKind.LLVMConstantPointerNullValueKind || kind == LLVMValueKind.LLVMUndefValueValueKind) {
                value = toValue(this, nativeValue).build();
            } else if (kind == LLVMValueKind.LLVMGlobalVariableValueKind) {
                ValueDto.ValueDtoBuilder builder = toValue(this, nativeValue);
                SWIGTYPE_p_LLVMOpaqueValue nativeInitializer = bitreader.LLVMGetInitializer(nativeValue);

                if (nativeInitializer != null) {
                    builder.operands(Collections.singletonList(getValueRef(nativeInitializer)));
                }

                value = builder.build();
            } else if (kind == LLVMValueKind.LLVMConstantIntValueKind) {
                value = toValue(this, nativeValue).intValue(bitreader.LLVMConstIntGetSExtValue(nativeValue)).build();
            } else if (kind == LLVMValueKind.LLVMConstantFPValueKind) {
                value = toValue(this, nativeValue).fpValue(bitreader.GetConstantFPDoubleValue(nativeValue)).build();
            } else if (kind == LLVMValueKind.LLVMConstantExprValueKind) {
                value = toValue(this, nativeValue).opcode(bitreader.LLVMGetConstOpcode(nativeValue).toString()).operands(getOperands(this, nativeValue)).build(); // todo add expr info
            } else if (kind == LLVMValueKind.LLVMMetadataAsValueValueKind || kind == LLVMValueKind.LLVMConstantAggregateZeroValueKind) {
                // trivial support (not using in clients yet)
                value = toValue(this, nativeValue).build();
            } else {
                throw new IllegalArgumentException("Unsupported type: " + kind);
            }

            valueRef = new ValueRefDto(ValueRefDto.ValueRefKind.Global, globalValues.size());
            globalValues.add(value);
            globalValueIds.put(nativeValue, valueRef);

            return valueRef;
        }

        @Override
        public int getTypeId(SWIGTYPE_p_LLVMOpaqueType type) {
            Integer id = typeIds.get(type);
            if (id != null) {
                return id;
            }

            id = typeIds.size();
            typeIds.put(type, id); // storing id first to handle circular references (linked list node types, etc)
            types.put(id, typeParser.parse(this, type));

            return id;
        }

        public Collection<TypeDto> getTypes() {
            return types.values();
        }
    }
}
