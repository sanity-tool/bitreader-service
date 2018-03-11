package ru.urururu.bitreaderservice.cpp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.urururu.bitreaderservice.dto.*;
import ru.urururu.sanity.cpp.llvm.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class NativeBytecodeParser {
    @Autowired
    private List<ParserListener> parserListeners;

    public ModuleDto parse(byte[] bitcode) {
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("sanity", ".bc");
            Files.write(tempFile, bitcode);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        SWIGTYPE_p_LLVMOpaqueModule m = bitreader.parse(tempFile.toFile().getAbsolutePath());

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
        ParseContext ctx = new ModuleParseContext();

        List<FunctionDto> functions = new ArrayList<>();

        SWIGTYPE_p_LLVMOpaqueValue nativeFunction = bitreader.LLVMGetFirstFunction(nativeModule);
        while (nativeFunction != null) {
            try {
                if (bitreader.LLVMGetFirstBasicBlock(nativeFunction) != null) {
                    functions.add(toFunction(ctx, nativeFunction));
                } else {
                    // todo store some info?
                }
            } catch (Exception e) {
                throw new IllegalStateException("Can't parse function: " + bitreader.LLVMGetValueName(nativeFunction));
            }

            nativeFunction = bitreader.LLVMGetNextFunction(nativeFunction);
        }

        return new ModuleDto(functions);
    }

    private FunctionDto toFunction(ParseContext ctx, SWIGTYPE_p_LLVMOpaqueValue nativeFunction) {
        Map<SWIGTYPE_p_LLVMOpaqueBasicBlock, BlockDto> blocks = new LinkedHashMap<>();

        SWIGTYPE_p_LLVMOpaqueBasicBlock nativeBlock = bitreader.LLVMGetFirstBasicBlock(nativeFunction);
        while (nativeBlock != null) {
            blocks.put(nativeBlock, toBlock(ctx, nativeBlock));

            nativeBlock = bitreader.LLVMGetNextBasicBlock(nativeBlock);
        }

        BlockDto entryBlock = blocks.get(bitreader.LLVMGetEntryBasicBlock(nativeFunction));

        return new FunctionDto(blocks.values(), instanceIndexOf(blocks.values(), entryBlock));
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

    private ValueDto toValue(ParseContext ctx, SWIGTYPE_p_LLVMOpaqueValue nativeValue) {
        return new ValueDto(bitreader.LLVMGetValueKind(nativeValue).toString());
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

    static class ModuleParseContext implements ParseContext {
        Map<SWIGTYPE_p_LLVMOpaqueType, TypeDto> types = new LinkedHashMap<>();
    }
}
