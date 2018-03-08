package ru.urururu.bitreaderservice.cpp;

import ru.urururu.sanity.cpp.llvm.SWIGTYPE_p_LLVMOpaqueValue;
import ru.urururu.sanity.cpp.llvm.bitreader;

import java.util.stream.LongStream;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class LlvmUtils {
    static boolean checkTag(SWIGTYPE_p_LLVMOpaqueValue node, String stringMarker, long... tags) {
        if (bitreader.LLVMIsAMDNode(node) == null) {
            return false;
        }
        SWIGTYPE_p_LLVMOpaqueValue maybeTag = bitreader.LLVMGetOperand(node, 0);
        if (bitreader.LLVMIsAConstantInt(maybeTag) != null) {
            long val = bitreader.LLVMConstIntGetSExtValue(maybeTag);
            return LongStream.of(tags).anyMatch(tag -> tag == val);
        }
        if (stringMarker != null && bitreader.LLVMIsAMDString(maybeTag) != null) {
            String mdString = bitreader.SAGetMDString(maybeTag);
            return mdString.equals(stringMarker);
        }
        return false;
    }
}
