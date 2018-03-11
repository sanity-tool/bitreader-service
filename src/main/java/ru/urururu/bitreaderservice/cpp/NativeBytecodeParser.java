package ru.urururu.bitreaderservice.cpp;

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

        Map<Integer, FunctionDto> functions = new LinkedHashMap<>();

        SWIGTYPE_p_LLVMOpaqueValue nativeFunction = bitreader.LLVMGetFirstFunction(nativeModule);
        while (nativeFunction != null) {
            try {
                if (bitreader.LLVMGetFirstBasicBlock(nativeFunction) != null) {
                    functions.put(ctx.getGlobalValueId(nativeFunction), toFunction(ctx, nativeFunction));
                } else {
                    // todo store some info?
                }
            } catch (Exception e) {
                throw new IllegalStateException("Can't parse function: " + bitreader.LLVMGetValueName(nativeFunction), e);
            }

            nativeFunction = bitreader.LLVMGetNextFunction(nativeFunction);
        }

        return new ModuleDto(functions.values(), ctx.getTypes());
    }

    private FunctionDto toFunction(ParseContext ctx, SWIGTYPE_p_LLVMOpaqueValue nativeFunction) {
        List<ValueDto> params = new ArrayList<>();
        SWIGTYPE_p_LLVMOpaqueValue nativeParam = bitreader.LLVMGetFirstParam(nativeFunction);
        while (nativeParam != null) {
            params.add(new ValueDto(bitreader.LLVMGetValueKind(nativeParam).toString(),
                    bitreader.LLVMGetValueName(nativeParam), ctx.getTypeId(bitreader.LLVMTypeOf(nativeParam))));
            nativeParam = bitreader.LLVMGetNextParam(nativeParam);
        }

        Map<SWIGTYPE_p_LLVMOpaqueBasicBlock, BlockDto> blocks = new LinkedHashMap<>();

        SWIGTYPE_p_LLVMOpaqueBasicBlock nativeBlock = bitreader.LLVMGetFirstBasicBlock(nativeFunction);
        while (nativeBlock != null) {
            blocks.put(nativeBlock, toBlock(ctx, nativeBlock));

            nativeBlock = bitreader.LLVMGetNextBasicBlock(nativeBlock);
        }

        BlockDto entryBlock = blocks.get(bitreader.LLVMGetEntryBasicBlock(nativeFunction));

        return new FunctionDto(blocks.values(), instanceIndexOf(blocks.values(), entryBlock), params);
    }

    private BlockDto toBlock(ParseContext ctx, SWIGTYPE_p_LLVMOpaqueBasicBlock nativeBlock) {
        Map<SWIGTYPE_p_LLVMOpaqueValue, InstructionDto> instructions = new LinkedHashMap<>();

        SWIGTYPE_p_LLVMOpaqueValue nativeInstruction = bitreader.LLVMGetFirstInstruction(nativeBlock);
        while (nativeInstruction != null) {
            instructions.put(nativeInstruction, toInstruction(ctx, nativeInstruction));

            nativeInstruction = bitreader.LLVMGetNextInstruction(nativeInstruction);
        }
        return new BlockDto(instructions.values());
    }

    private InstructionDto toInstruction(ParseContext ctx, SWIGTYPE_p_LLVMOpaqueValue nativeInstruction) {
        switch (bitreader.LLVMGetInstructionOpcode(nativeInstruction).toString()) {
            default:
                int numOperands = bitreader.LLVMGetNumOperands(nativeInstruction);
                List<ValueDto> operands = new ArrayList<>();
                for (int i = 0; i < numOperands; i++) {
                    operands.add(toValue(ctx, bitreader.LLVMGetOperand(nativeInstruction, i)));
                }

                return new InstructionDto(bitreader.LLVMGetInstructionOpcode(nativeInstruction).toString(), operands);
        }
    }

    private static ValueDto toValue(ParseContext ctx, SWIGTYPE_p_LLVMOpaqueValue nativeValue) {
        return new ValueDto(bitreader.LLVMGetValueKind(nativeValue).toString(),
                bitreader.LLVMGetValueName(nativeValue), ctx.getTypeId(bitreader.LLVMTypeOf(nativeValue)));
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
        Map<SWIGTYPE_p_LLVMOpaqueValue, Integer> globalValueIds = new HashMap<>();
        Map<Integer, TypeDto> types = new TreeMap<>();
        Map<SWIGTYPE_p_LLVMOpaqueType, Integer> typeIds = new HashMap<>();

        @Override
        public int getGlobalValueId(SWIGTYPE_p_LLVMOpaqueValue value) {
            return globalValueIds.computeIfAbsent(value, v -> {
                globalValues.add(toValue(this, v));
                return globalValues.size() - 1;
            });
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
