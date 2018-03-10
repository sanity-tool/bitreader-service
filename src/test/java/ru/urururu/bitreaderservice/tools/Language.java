package ru.urururu.bitreaderservice.tools;

import java.util.*;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public enum Language {
    C("c"),
    Cpp("cpp"),
    ObjectiveC("m"),
    Swift("swift"),
    Rust("rs"),
    IR("ll"),
    ;

    Set<String> extensions;

    Language(String... extensions) {
        this.extensions = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(extensions)));
    }

    public Set<String> getExtensions() {
        return extensions;
    }

    private static final Map<String, Language> byExtensions = new HashMap<>();

    static {
        for (Language language : Language.values()) {
            for (String extension : language.getExtensions()) {
                Language old = byExtensions.put(extension, language);
                if (old != null) {
                    throw new IllegalStateException("Many languages for extension '" + extension + "': " + old + ", " + language);
                }
            }
        }
    }

    public static Language getByExtension(String extension) {
        return byExtensions.get(extension);
    }
}
