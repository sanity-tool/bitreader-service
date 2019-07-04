package ru.urururu.bitreaderservice;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import junit.framework.TestSuite;
import ru.urururu.bitreaderservice.cpp.Parser;
import ru.urururu.bitreaderservice.dto.ModuleDto;
import ru.urururu.bitreaderservice.dto.SourceRefDto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.io.File;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class ParserTests extends TestHelper {
    public static TestSuite suite() {
        TestSuite suite = new TestSuite("parser");

        new ParserTests().fillWithTests(suite, "parser");

        return suite;
    }

    @Override
    public void runTest(String unit, Path pathToExpected) throws Exception {
        Parser parser = context.getBean(Parser.class);

        ModuleDto testResult = parser.parse(unit, (prefix, suffix) -> getDebugPath(unit, prefix, suffix), true);

        if (testResult == null) {
            throw new IllegalStateException(unit);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PrintStream ps = new PrintStream(baos);

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        SimpleModule relativeFilesModule = new SimpleModule("fileSerialization");
        relativeFilesModule.setSerializerModifier(new SourceRefSerializerModifier());
        mapper.registerModule(relativeFilesModule);

        mapper.writeValue(ps, testResult);

        String actual = baos.toString();
        check(pathToExpected, actual);
    }

    public class SourceRefSerializer extends JsonSerializer<SourceRefDto> {
        private final JsonSerializer<Object> defaultSerializer;

        SourceRefSerializer(JsonSerializer<Object> defaultSerializer) {
            this.defaultSerializer = defaultSerializer;
        }

        @Override
        public void serialize(SourceRefDto value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            SourceRefDto relativeValue = new SourceRefDto(
                    relativize(value.getFile()),
                    value.getLine()
            );

            defaultSerializer.serialize(relativeValue, gen, provider);
        }
    }

    private String relativize(String filename) {
        int i = filename.indexOf("src/src");
        if (i != -1) {
            // workaround for rustc tests
            return "<rustc-src>" + filename.substring(i + 3); // taking substring inclusive to last "src"
        }
        if (filename.startsWith("libcore")) {
            // workaround for rustc tests
            return "<rustc-src>/src/" + filename;
        }

        File file = new File(filename);
        if (file.isAbsolute()) {
            return TestHelper.TESTS_PATH.relativize(file.toPath()).toString();
        }

        return filename;
    }

    public class SourceRefSerializerModifier extends BeanSerializerModifier {
        @Override
        public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
            if (beanDesc.getBeanClass() == SourceRefDto.class) {
                return new SourceRefSerializer((JsonSerializer<Object>) serializer);
            }
            return serializer;
        }
    }
}
