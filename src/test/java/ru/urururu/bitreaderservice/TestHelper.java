package ru.urururu.bitreaderservice;

import junit.framework.Assert;
import junit.framework.ComparisonFailure;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.urururu.bitreaderservice.cpp.tools.Language;
import ru.urururu.bitreaderservice.cpp.tools.Tool;
import ru.urururu.bitreaderservice.cpp.tools.ToolFactory;
import ru.urururu.bitreaderservice.utils.FileWrapper;
import ru.urururu.bitreaderservice.utils.TempFileWrapper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
abstract class TestHelper {
    static ApplicationContext context = new AnnotationConfigApplicationContext("ru.urururu.bitreaderservice.cpp");

    private static final String BASE = System.getProperty("TEST_RESOURCES_ROOT");
    private static Path TESTS_PATH = Paths.get(BASE);
    private static String FAILURES_DIR = System.getProperty("TEST_FAILURES_ROOT");
    private static String DEBUG_DIR = System.getProperty("TEST_DEBUG_ROOT");
    private static final BidiMap<Language, String> languageDirs = new DualHashBidiMap<>();
    private static final String LANG = System.getProperty("TESTED_LANG");
    private static final String FILTER = StringUtils.defaultString(System.getenv("TEST_FILTER"), "");
    private static final Logger LOGGER = Logger.getLogger(TestHelper.class.getSimpleName());

    private static ToolFactory toolFactory;

    static {
        toolFactory = context.getBean(ToolFactory.class);

        languageDirs.put(Language.C, "c");
        languageDirs.put(Language.Cpp, "cpp");
        languageDirs.put(Language.ObjectiveC, "o-c");
    }

    void fillWithTests(TestSuite suite, String path) {
        LOGGER.info("Languages: " + toolFactory.getLanguages());

        File root = new File(BASE, path);
        fillWithTests(suite, root);

        if (suite.countTestCases() == 0) {
            throw new IllegalStateException("No files in " + root.getAbsolutePath());
        }
    }

    private void fillWithTests(TestSuite suite, File file) {
        File[] files = file.listFiles();

        if (files == null) {
            return;
        }

        for (final File f : files) {
            if (matches(f)) {
                Tool testTool;
                if (f.isDirectory()) {
                    String name = f.getName();
                    Language language = languageDirs.getKey(name);
                    testTool = toolFactory.get(language);
                } else {
                    testTool = toolFactory.get(FilenameUtils.getExtension(f.getAbsolutePath()));
                }

                String absolutePath = f.getAbsolutePath();
                Path pathToExpected = getPathToExpected(f, testTool);

                suite.addTest(new TestCase(f.getName()) {
                    @Override
                    protected void runTest() throws Throwable {
                        TestHelper.this.runTest(absolutePath, pathToExpected);
                    }
                });
            } else if (f.isDirectory()) {
                TestSuite inner = new TestSuite(f.getName());
                fillWithTests(inner, f);
                suite.addTest(inner);
            }
        }
    }

    private Path getPathToExpected(File testFile, Tool tool) {
        if (tool != null) {
            List<String> versionIds = tool.getVersionIds();

            for (String versionId : versionIds) {
                Path pathToExpected = Paths.get(testFile.getAbsolutePath() + '.' + versionId + ".expected.json");
                if (pathToExpected.toFile().exists()) {
                    return pathToExpected;
                }
            }
        }
        return Paths.get(testFile.getAbsolutePath() + ".expected.json");
    }

    private boolean matches(File file) {
        return file.getName().contains(FILTER) && isSupportedByExtension(file);
    }

    private boolean isSupportedByExtension(File file) {
        LOGGER.fine("isSupportedByExtension: " + file.getName());
        return isExtensionSupported(FilenameUtils.getExtension(file.getName()));
    }

    private boolean isExtensionSupported(String extension) {
        LOGGER.fine("isExtensionSupported: " + extension);
        return isLanguageSupported(Language.getByExtension(extension));
    }

    private boolean isLanguageSupported(Language language) {
        LOGGER.fine("isLanguageSupported: " + language);
        if (language == null) {
            return false;
        }
        if (StringUtils.isNotEmpty(LANG)) {
            return language.toString().equals(LANG);
        }
        return toolFactory.getLanguages().contains(language);
    }

    public abstract void runTest(String unit, Path pathToExpected) throws Exception;

    void check(Path pathToExpected, String actual) throws IOException {
        try {
            byte[] bytes = Files.readAllBytes(pathToExpected);
            String expected = new String(bytes, Charset.defaultCharset());

            Assert.assertEquals(expected, actual);
        } catch (ComparisonFailure e) {
            if (FAILURES_DIR != null) {
                Path resultSubPath = TESTS_PATH.relativize(pathToExpected);
                Path failuresPath = Paths.get(FAILURES_DIR);

                Path expectedDir = failuresPath.resolve("expected");
                Path pathToExpected2 = expectedDir.resolve(resultSubPath);
                pathToExpected2.getParent().toFile().mkdirs();
                Files.copy(pathToExpected, pathToExpected2, StandardCopyOption.REPLACE_EXISTING);

                Path actualDir = failuresPath.resolve("actual");
                Path pathToActual = actualDir.resolve(resultSubPath);
                pathToActual.getParent().toFile().mkdirs();
                Files.write(pathToActual, actual.getBytes());
            }

            throw e;
        } catch (NoSuchFileException e) {
            Files.write(pathToExpected, actual.getBytes());
            Assert.fail("File " + pathToExpected + " not found, but I've created it for you anyways.");
        }
    }

    FileWrapper getDebugPath(String unit, String prefix, String suffix) {
        if (DEBUG_DIR != null) {
            Path debugPath = Paths.get(DEBUG_DIR);

            Path basePath = Paths.get(BASE);

            Path unitPath = Paths.get(unit);

            Path unitDir = debugPath.resolve(basePath.relativize(unitPath));
            File unitDirFile = unitDir.toFile();
            unitDirFile.mkdirs();

            return new FileWrapper(unitDir.resolve(prefix + suffix).toFile()) {
                @Override
                public void close() throws IOException {
                }
            };
        }

        return new TempFileWrapper(prefix, suffix);
    }
}
