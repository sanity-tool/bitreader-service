package ru.urururu.bitreaderservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import junit.framework.TestSuite;
import ru.urururu.bitreaderservice.cpp.Parser;
import ru.urururu.bitreaderservice.dto.ModuleDto;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;

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

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PrintStream ps = new PrintStream(baos);

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        mapper.writeValue(ps, testResult);

        String actual = baos.toString();
        check(pathToExpected, actual);
    }
}
