package ru.urururu.bitreaderservice.cpp.tools;

import org.apache.commons.lang3.SystemUtils;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
class Clang extends Tool {
    Clang(String executable, String version) {
        super(executable, version);
    }

    @Override
    Set<Language> getLanguages() {
        return SystemUtils.IS_OS_MAC ? EnumSet.of(Language.C, Language.Cpp, Language.ObjectiveC) : EnumSet.of(Language.C, Language.Cpp);
    }

    @Override
    public String[] createParameters(String filename, String objFile) {
        if (filename.endsWith("hello.m")) {
            return new String[]{executable, "-framework", "Foundation", filename, "-c", "-emit-llvm", "-femit-all-decls", "-g", "-o", objFile};
        }

        return new String[]{executable, filename, "-c", "-emit-llvm", "-femit-all-decls", "-g", "-o", objFile};
    }

    @Override
    public String[] createDebugParameters(String filename, String debugFile) {
        return new String[]{executable, filename, "-S", "-emit-llvm", "-femit-all-decls", "-g", "-o", debugFile};
    }

    @Override
    List<String> evaluateVersionIds(String version) {
        String clangVersion = "clang version";
        if (version.startsWith(clangVersion)) {
            return createVersionsFamily("clang", version.substring(clangVersion.length(), version.indexOf('(')).trim());
        } else {
            String appleLlvmVersion = "Apple LLVM version";
            if (version.startsWith(appleLlvmVersion)) {
                return createVersionsFamily("allvm", version.substring(appleLlvmVersion.length(), version.indexOf('(')).trim());
            }
        }

        return super.evaluateVersionIds(version);
    }
}
