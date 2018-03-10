package ru.urururu.bitreaderservice.cpp.tools;

import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class ToolFactory {
    private final Set<Tool> tools = new HashSet<>();
    private final Map<String, Tool> byExtensions = new HashMap<>();
    private final Map<Language, Tool> byLanguages = new EnumMap<>(Language.class);

    public ToolFactory() throws InterruptedException {
        Tool.tryCreate("sanity.clang", "clang", Clang::new).ifPresent(this::addTool);
        Tool.tryCreate("sanity.swiftc", "swiftc", Swift::new).ifPresent(this::addTool);
        Tool.tryCreate("sanity.rustc", "rustc", Rust::new).ifPresent(this::addTool);

        Tool.tryCreate("sanity.llvm-as", "llvm-as", LlvmAs::new).ifPresent(this::addTool);

        if (tools.isEmpty()) {
            throw new IllegalStateException("No tools found");
        }
    }

    private void addTool(Tool tool) {
        if (!tools.add(tool)) {
           throw new IllegalStateException("Duplicate tool: " + tool);
        }

        for (Language language : tool.getLanguages()) {
            byLanguages.merge(language, tool, (t1, t2) -> {
                throw new IllegalStateException("More that one tool for " + language + ": " + t1 + " and " + t2);
            });

            for (String extension : language.getExtensions()) {
                byExtensions.merge(extension, tool, (t1, t2) -> {
                    throw new IllegalStateException("More that one tool for ." + extension + ": " + t1 + " and " + t2);
                });
            }
        }
    }

    public Set<Language> getLanguages() {
        return byLanguages.keySet();
    }

    public Set<Tool> getTools() {
        return Collections.unmodifiableSet(tools);
    }

    public Set<String> getExtensions() {
        return byExtensions.keySet();
    }

    public Tool get(String extension) {
        return byExtensions.computeIfAbsent(extension, key -> {
            throw new IllegalArgumentException("No tool for " + key);
        });
    }

    public Tool get(Language language) {
        return byLanguages.get(language);
    }
}
