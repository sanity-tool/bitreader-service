package ru.urururu.bitreaderservice.cpp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.urururu.sanity.api.InstructionParser;
import ru.urururu.sanity.api.cfg.*;
import ru.urururu.sanity.cpp.llvm.*;
import ru.urururu.util.FinalMap;
import ru.urururu.util.Iterables;

import java.util.*;


/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class NativeInstructionParser extends InstructionParser<SWIGTYPE_p_LLVMOpaqueType,
        SWIGTYPE_p_LLVMOpaqueValue, SWIGTYPE_p_LLVMOpaqueValue, SWIGTYPE_p_LLVMOpaqueBasicBlock, NativeCfgBuildingCtx> {
    @Autowired
    ConstCache constants;

    private Map<LLVMOpcode, OpcodeParser> opcodeParsers;

    private OpcodeParser defaultParser = new AbstractParser() {
        @Override
        public Set<LLVMOpcode> getOpcodes() {
            return Collections.emptySet();
        }
    };

    public NativeInstructionParser() {
        this.opcodeParsers = FinalMap.createHashMap();

        List<OpcodeParser> parsersOtu = new ArrayList<>();
        parsersOtu.add(new StoreParser());
        parsersOtu.add(new LoadParser());
        parsersOtu.add(new RetParser());
        parsersOtu.add(new AllocaParser());
        parsersOtu.add(new GetElementPtrParser());
        parsersOtu.add(new ExtractValueParser());
        parsersOtu.add(new CallParser());
        parsersOtu.add(new PhiParser());
        parsersOtu.add(new BrParser());
        parsersOtu.add(new SwitchParser());
        parsersOtu.add(new BinaryOperationParser());
        parsersOtu.add(new BitCastParser());
        parsersOtu.add(new CastParser());

        parsersOtu.add(new ICmpParser());
        parsersOtu.add(new FCmpParser());

        for (OpcodeParser parser : parsersOtu) {
            for (LLVMOpcode opcode : parser.getOpcodes()) {
                opcodeParsers.put(opcode, parser);
            }
        }
    }

    protected Cfe doParse(NativeCfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
        OpcodeParser parser = opcodeParsers.getOrDefault(bitreader.LLVMGetInstructionOpcode(instruction), defaultParser);

        return parser.parse(ctx, instruction);
    }

    public RValue parseValue(NativeCfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
        OpcodeParser parser = opcodeParsers.getOrDefault(bitreader.LLVMGetInstructionOpcode(instruction), defaultParser);
        return parser.parseValue(ctx, instruction);
    }

    public RValue parseConst(NativeCfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue constant) {
        OpcodeParser parser = opcodeParsers.getOrDefault(bitreader.LLVMGetConstOpcode(constant), defaultParser);
        return parser.parseConst(ctx, constant);
    }

    private interface OpcodeParser {
        Set<LLVMOpcode> getOpcodes();

        Cfe parse(NativeCfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction);

        RValue parseValue(NativeCfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction);

        RValue parseConst(NativeCfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue constant);
    }

    private static abstract class AbstractParser implements OpcodeParser {
        @Override
        public Set<LLVMOpcode> getOpcodes() {
            return Collections.singleton(getOpcode());
        }

        public LLVMOpcode getOpcode() {
            return null;
        }

        @Override
        public Cfe parse(NativeCfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            throw new IllegalStateException("opcode '" + bitreader.LLVMGetInstructionOpcode(instruction) + "' not supported");
        }

        @Override
        public RValue parseValue(NativeCfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            throw new IllegalStateException("opcode '" + bitreader.LLVMGetInstructionOpcode(instruction) + "' not supported");
        }

        @Override
        public RValue parseConst(NativeCfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue constant) {
            throw new IllegalStateException("opcode '" + bitreader.LLVMGetConstOpcode(constant) + "' not supported");
        }
    }

    private class StoreParser extends AbstractParser {
        @Override
        public LLVMOpcode getOpcode() {
            return LLVMOpcode.LLVMStore;
        }

        @Override
        public Cfe parse(NativeCfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            return createStore(ctx, instruction,
                    bitreader.LLVMGetOperand(instruction, 0), bitreader.LLVMGetOperand(instruction, 1));
        }
    }

    private class LoadParser extends AbstractParser {
        @Override
        public LLVMOpcode getOpcode() {
            return LLVMOpcode.LLVMLoad;
        }

        @Override
        public Cfe parse(NativeCfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            return null;
        }

        @Override
        public RValue parseValue(NativeCfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            return createLoad(ctx, bitreader.LLVMGetOperand(instruction, 0));
        }
    }

    private class RetParser extends AbstractParser {
        @Override
        public LLVMOpcode getOpcode() {
            return LLVMOpcode.LLVMRet;
        }

        @Override
        public Cfe parse(NativeCfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            if (bitreader.LLVMGetNumOperands(instruction) == 0) {
                return createReturn(ctx, instruction);
            }

            return createReturn(ctx, instruction, bitreader.LLVMGetOperand(instruction, 0));
        }
    }

    private static class AllocaParser extends AbstractParser {
        @Override
        public LLVMOpcode getOpcode() {
            return LLVMOpcode.LLVMAlloca;
        }

        @Override
        public Cfe parse(NativeCfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            return null;
        }

        @Override
        public RValue parseValue(NativeCfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            return ctx.getOrCreateTmpVar(instruction);
        }
    }

    private class GetElementPtrParser extends AbstractParser {
        @Override
        public LLVMOpcode getOpcode() {
            return LLVMOpcode.LLVMGetElementPtr;
        }

        @Override
        public Cfe parse(NativeCfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            return null;
        }

        @Override
        public RValue parseValue(NativeCfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            int operandsCount = bitreader.LLVMGetNumOperands(instruction);

            return getPointer(ctx, bitreader.LLVMGetOperand(instruction, 0), Iterables.indexed(
                    i -> bitreader.LLVMGetOperand(instruction, i + 1),
                    () -> operandsCount - 1));
        }

        @Override
        public RValue parseConst(NativeCfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue constant) {
            return parseValue(ctx, constant);
        }
    }

    private class ExtractValueParser extends AbstractParser {
        @Override
        public LLVMOpcode getOpcode() {
            return LLVMOpcode.LLVMExtractValue;
        }

        @Override
        public Cfe parse(NativeCfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            return null;
        }

        @Override
        public RValue parseValue(NativeCfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            // todo wtf? tests?
            RValue pointer = parsers.parseRValue(ctx, bitreader.LLVMGetOperand(instruction, 0));

            pointer = getPointer(pointer, constants.get(0, null));

            int operandsCount = bitreader.LLVMGetNumOperands(instruction);

            int i = 1;
            while (i < operandsCount) {
                pointer = getPointer(pointer, parsers.parseRValue(ctx, bitreader.LLVMGetOperand(instruction, i)));
                i++;
            }

            return pointer;
        }

        @Override
        public RValue parseConst(NativeCfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue constant) {
            return parseValue(ctx, constant);
        }
    }

    private class CallParser extends AbstractParser {
        @Override
        public LLVMOpcode getOpcode() {
            return LLVMOpcode.LLVMCall;
        }

        @Override
        public Cfe parse(NativeCfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            int argLen = bitreader.LLVMGetNumOperands(instruction) - 1;

            return createCall(ctx, instruction, bitreader.LLVMGetOperand(instruction, argLen),
                    Iterables.indexed(i -> bitreader.LLVMGetOperand(instruction, i), () -> argLen));
        }

        @Override
        public RValue parseValue(NativeCfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            return ctx.getTmpVar(instruction);
        }
    }

    private class PhiParser extends AbstractParser {
        @Override
        public LLVMOpcode getOpcode() {
            return LLVMOpcode.LLVMPHI;
        }

        @Override
        public Cfe parse(NativeCfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            return null;
        }

        @Override
        public RValue parseValue(NativeCfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            return ctx.getTmpVar(instruction);
        }
    }

    private class BrParser extends AbstractParser {
        @Override
        public LLVMOpcode getOpcode() {
            return LLVMOpcode.LLVMBr;
        }

        @Override
        public Cfe parse(NativeCfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            if (bitreader.LLVMGetNumOperands(instruction) == 1) {
                return createGoto(ctx, instruction, bitreader.LLVMGetOperand(instruction, 0));
            }
            return createIf(ctx, instruction, bitreader.LLVMGetOperand(instruction, 0),
                    bitreader.LLVMGetOperand(instruction, 2), bitreader.LLVMGetOperand(instruction, 1));
        }
    }

    private class SwitchParser extends AbstractParser {
        @Override
        public LLVMOpcode getOpcode() {
            return LLVMOpcode.LLVMSwitch;
        }

        @Override
        public Cfe parse(NativeCfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            List<SWIGTYPE_p_LLVMOpaqueValue> values = new ArrayList<>();
            List<SWIGTYPE_p_LLVMOpaqueValue> labels = new ArrayList<>();

            int i = 2;
            while (i + 1 < bitreader.LLVMGetNumOperands(instruction)) {
                values.add(bitreader.LLVMGetOperand(instruction, i++));
                labels.add(bitreader.LLVMGetOperand(instruction, i++));
            }

            return createSwitch(ctx, instruction, bitreader.LLVMGetOperand(instruction, 0), bitreader.LLVMGetOperand(instruction, 1), values, labels);
        }
    }

    private class BinaryOperationParser extends AbstractParser {
        private final Map<LLVMOpcode, BinaryExpression.Operator> opcodeOperatorMap;

        BinaryOperationParser() {
            opcodeOperatorMap = FinalMap.createHashMap();
            opcodeOperatorMap.put(LLVMOpcode.LLVMAdd, BinaryExpression.Operator.Add);
            opcodeOperatorMap.put(LLVMOpcode.LLVMFAdd, BinaryExpression.Operator.Add);
            opcodeOperatorMap.put(LLVMOpcode.LLVMSub, BinaryExpression.Operator.Sub);
            opcodeOperatorMap.put(LLVMOpcode.LLVMFSub, BinaryExpression.Operator.Sub);
            opcodeOperatorMap.put(LLVMOpcode.LLVMMul, BinaryExpression.Operator.Mul);
            opcodeOperatorMap.put(LLVMOpcode.LLVMFMul, BinaryExpression.Operator.Mul);
            opcodeOperatorMap.put(LLVMOpcode.LLVMUDiv, BinaryExpression.Operator.Div);
            opcodeOperatorMap.put(LLVMOpcode.LLVMSDiv, BinaryExpression.Operator.Div);
            opcodeOperatorMap.put(LLVMOpcode.LLVMFDiv, BinaryExpression.Operator.Div);
            opcodeOperatorMap.put(LLVMOpcode.LLVMURem, BinaryExpression.Operator.Rem);
            opcodeOperatorMap.put(LLVMOpcode.LLVMSRem, BinaryExpression.Operator.Rem);
            opcodeOperatorMap.put(LLVMOpcode.LLVMFRem, BinaryExpression.Operator.Rem);

            opcodeOperatorMap.put(LLVMOpcode.LLVMAnd, BinaryExpression.Operator.And);
            opcodeOperatorMap.put(LLVMOpcode.LLVMOr, BinaryExpression.Operator.Or);
            opcodeOperatorMap.put(LLVMOpcode.LLVMXor, BinaryExpression.Operator.Xor);

            opcodeOperatorMap.put(LLVMOpcode.LLVMShl, BinaryExpression.Operator.ShiftLeft);
            opcodeOperatorMap.put(LLVMOpcode.LLVMLShr, BinaryExpression.Operator.ShiftRight);
            opcodeOperatorMap.put(LLVMOpcode.LLVMAShr, BinaryExpression.Operator.ShiftRight);
        }

        @Deprecated
        public BinaryOperationParser(LLVMOpcode opcode, BinaryExpression.Operator operator) {
            opcodeOperatorMap = Collections.singletonMap(opcode, operator);
        }

        @Override
        public Set<LLVMOpcode> getOpcodes() {
            return opcodeOperatorMap.keySet();
        }

        @Override
        public Cfe parse(NativeCfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            return createBinaryAssignment(ctx, instruction,
                    bitreader.LLVMGetOperand(instruction, 0),
                    opcodeOperatorMap.get(bitreader.LLVMGetInstructionOpcode(instruction)),
                    bitreader.LLVMGetOperand(instruction, 1));
        }

        @Override
        public RValue parseValue(NativeCfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            return ctx.getTmpVar(instruction);
        }
    }

    private class BitCastParser extends AbstractParser {
        @Override
        public LLVMOpcode getOpcode() {
            return LLVMOpcode.LLVMBitCast;
        }

        @Override
        public Cfe parse(NativeCfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            return createBitCast(ctx, instruction, bitreader.LLVMGetOperand(instruction, 0));
        }

        @Override
        public RValue parseValue(NativeCfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            return ctx.getTmpVar(instruction);
        }

        @Override
        public RValue parseConst(NativeCfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue constant) {
            return parsers.parseRValue(ctx, bitreader.LLVMGetOperand(constant, 0));
        }
    }

    private class CastParser extends AbstractParser {
        @Override
        public Set<LLVMOpcode> getOpcodes() {
            return new HashSet<>(Arrays.asList(LLVMOpcode.LLVMSExt, LLVMOpcode.LLVMZExt, LLVMOpcode.LLVMTrunc, LLVMOpcode.LLVMSIToFP));
        }

        @Override
        public Cfe parse(NativeCfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            // todo think about source range comparison. if different - it's better to have tmp var assignment to preserve source reference.
            return null;
        }

        @Override
        public RValue parseValue(NativeCfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            // just return it's operand, it's smaller, so it will fit.
            return parsers.parseRValue(ctx, bitreader.LLVMGetOperand(instruction, 0));
        }
    }

    private class ICmpParser extends AbstractParser {
        Map<LLVMIntPredicate, BinaryExpression.Operator> predicateOperatorMap = FinalMap.createHashMap();

        ICmpParser() {
            predicateOperatorMap.put(LLVMIntPredicate.LLVMIntSLT, BinaryExpression.Operator.Lt);
            predicateOperatorMap.put(LLVMIntPredicate.LLVMIntSLE, BinaryExpression.Operator.Le);
            predicateOperatorMap.put(LLVMIntPredicate.LLVMIntSGT, BinaryExpression.Operator.Gt);
            predicateOperatorMap.put(LLVMIntPredicate.LLVMIntSGE, BinaryExpression.Operator.Ge);

            predicateOperatorMap.put(LLVMIntPredicate.LLVMIntULT, BinaryExpression.Operator.Lt);
            predicateOperatorMap.put(LLVMIntPredicate.LLVMIntULE, BinaryExpression.Operator.Le);
            predicateOperatorMap.put(LLVMIntPredicate.LLVMIntUGT, BinaryExpression.Operator.Gt);
            predicateOperatorMap.put(LLVMIntPredicate.LLVMIntUGE, BinaryExpression.Operator.Ge);

            predicateOperatorMap.put(LLVMIntPredicate.LLVMIntEQ, BinaryExpression.Operator.Eq);
            predicateOperatorMap.put(LLVMIntPredicate.LLVMIntNE, BinaryExpression.Operator.Ne);
        }

        @Override
        public LLVMOpcode getOpcode() {
            return LLVMOpcode.LLVMICmp;
        }

        @Override
        public Cfe parse(NativeCfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            return createBinaryAssignment(ctx, instruction,
                    bitreader.LLVMGetOperand(instruction, 0),
                    predicateOperatorMap.get(bitreader.LLVMGetICmpPredicate(instruction)),
                    bitreader.LLVMGetOperand(instruction, 1));
        }

        @Override
        public RValue parseValue(NativeCfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            return ctx.getTmpVar(instruction);
        }
    }

    private class FCmpParser extends AbstractParser {
        Map<LLVMRealPredicate, BinaryExpression.Operator> predicateOperatorMap = FinalMap.createHashMap();

        FCmpParser() {
            predicateOperatorMap.put(LLVMRealPredicate.LLVMRealOLT, BinaryExpression.Operator.Lt);
            predicateOperatorMap.put(LLVMRealPredicate.LLVMRealOLE, BinaryExpression.Operator.Le);
            predicateOperatorMap.put(LLVMRealPredicate.LLVMRealOGT, BinaryExpression.Operator.Gt);
            predicateOperatorMap.put(LLVMRealPredicate.LLVMRealOGE, BinaryExpression.Operator.Ge);

            predicateOperatorMap.put(LLVMRealPredicate.LLVMRealULT, BinaryExpression.Operator.Lt);
            predicateOperatorMap.put(LLVMRealPredicate.LLVMRealULE, BinaryExpression.Operator.Le);
            predicateOperatorMap.put(LLVMRealPredicate.LLVMRealUGT, BinaryExpression.Operator.Gt);
            predicateOperatorMap.put(LLVMRealPredicate.LLVMRealUGE, BinaryExpression.Operator.Ge);

            predicateOperatorMap.put(LLVMRealPredicate.LLVMRealOEQ, BinaryExpression.Operator.Eq);
            predicateOperatorMap.put(LLVMRealPredicate.LLVMRealONE, BinaryExpression.Operator.Ne);

            predicateOperatorMap.put(LLVMRealPredicate.LLVMRealUEQ, BinaryExpression.Operator.Eq);
            predicateOperatorMap.put(LLVMRealPredicate.LLVMRealUNE, BinaryExpression.Operator.Ne);
        }

        @Override
        public LLVMOpcode getOpcode() {
            return LLVMOpcode.LLVMFCmp;
        }

        @Override
        public Cfe parse(NativeCfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            return createBinaryAssignment(ctx, instruction,
                    bitreader.LLVMGetOperand(instruction, 0),
                    predicateOperatorMap.get(bitreader.GetFCmpPredicate(instruction)),
                    bitreader.LLVMGetOperand(instruction, 1));
        }

        @Override
        public RValue parseValue(NativeCfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            return ctx.getTmpVar(instruction);
        }
    }
}
