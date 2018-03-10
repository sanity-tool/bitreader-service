package ru.urururu.bitreaderservice.tools;

import java.util.EnumSet;
import java.util.Set;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
class LlvmAs extends Tool {
    LlvmAs(String executable, String version) {
        super(executable, version);
    }

    @Override
    Set<Language> getLanguages() {
        return EnumSet.of(Language.IR);
    }

    @Override
    public String[] createParameters(String filename, String objFile) {
        return new String[]{executable, "-o=" + objFile, filename};
    }
}
