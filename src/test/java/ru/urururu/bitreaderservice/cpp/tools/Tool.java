package ru.urururu.bitreaderservice.cpp.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.BiFunction;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public abstract class Tool {
    private static final Logger LOGGER = LoggerFactory.getLogger(Tool.class);

    final String executable;
    private final List<String> versionIds;

    Tool(String executable, String version) {
        this.executable = executable;
        this.versionIds = evaluateVersionIds(version);

        LOGGER.info("executable = {}", executable);
        LOGGER.info("versionString = {}", version);
        LOGGER.info("versionIds = {}", versionIds);
    }

    abstract Set<Language> getLanguages();

    static Optional<Tool> tryCreate(String key, String def, BiFunction<String, String, Tool> factory) throws InterruptedException {
        String executable = System.getProperty(key);
        if (executable != null) {
            try {
                return create(executable, factory);
            } catch (IOException e) {
                throw new IllegalStateException("Explicitly specified tool creation failed", e);
            }
        }

        try {
            return create(def, factory);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private static Optional<Tool> create(String executable, BiFunction<String, String, Tool> factory) throws IOException, InterruptedException {
        String version;
        ProcessBuilder pb = new ProcessBuilder(executable, "--version");
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            version = reader.readLine();
        }

        pb.start().waitFor();
        return Optional.of(factory.apply(executable, version));
    }

    public abstract String[] createParameters(String filename, String objFile);

    public String[] createDebugParameters(String filename, String debugFile) {
        return null;
    }

    /**
     * @return version identifiers from most specific to more generic
     */
    public List<String> getVersionIds() {
        return versionIds;
    }

    List<String> evaluateVersionIds(String version) {
        LOGGER.warn("unknown version = {}", version);
        return Collections.singletonList("unknown");
    }

    protected List<String> createVersionsFamily(String prefix, String version) {
        String[] versionParts = version.split("\\.");

        String[] result = new String[versionParts.length];

        StringBuilder sb = new StringBuilder(prefix);
        for (int i = 0; i < versionParts.length; i++) {
            sb.append(versionParts[i]);
            result[result.length - 1 - i] = sb.toString();
        }

        return Collections.unmodifiableList(Arrays.asList(result));
    }
}
