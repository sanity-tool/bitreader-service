package ru.urururu.bitreaderservice.cpp.tools;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
class Rust extends Tool {
    Rust(String executable, String version) {
        super(executable, version);
    }

    @Override
    Set<Language> getLanguages() {
        return EnumSet.of(Language.Rust);
    }

    @Override
    public String[] createParameters(String filename, String objFile) {
        return new String[]{executable, "--crate-type=lib", "-g", "-A", "dead_code", "--emit=llvm-bc", "-o", objFile, filename};
    }

    @Override
    public String[] createDebugParameters(String filename, String debugFile) {
        return new String[]{executable, "--crate-type=lib", "-g", "-A", "dead_code", "--emit=llvm-ir", "-o", debugFile, filename};
    }

    @Override
    List<String> evaluateVersionIds(String version) {
        return createVersionsFamily("rustc", version.substring("rustc".length()).trim());
    }
}
