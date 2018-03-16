package ru.urururu.bitreaderservice;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import junit.framework.TestSuite;
import ru.urururu.bitreaderservice.cpp.Parser;
import ru.urururu.bitreaderservice.dto.ModuleDto;

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
        SimpleModule simpleModule = new SimpleModule("fileSerialization");
        simpleModule.addSerializer(File.class, new JsonSerializer<File>() {
            @Override
            public void serialize(File value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeString(value.toPath().relativize(TestHelper.TESTS_PATH).toString());
            }
        });
        mapper.registerModule(simpleModule);

        mapper.writeValue(ps, testResult);

        String actual = baos.toString();
        check(pathToExpected, actual);
    }
}
