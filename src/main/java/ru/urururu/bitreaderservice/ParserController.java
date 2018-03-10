package ru.urururu.bitreaderservice;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ru.urururu.bitreaderservice.cpp.NativeBytecodeParser;
import ru.urururu.bitreaderservice.dto.ModuleDto;

import java.io.IOException;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Api
@RestController
public class ParserController {
    @Autowired
    private NativeBytecodeParser parser;

    @RequestMapping(value = "/parse", consumes = "application/octet-stream", method = RequestMethod.POST)
    public ModuleDto parse(@RequestBody byte[] bitcode) throws IOException {
        return parser.parse(bitcode);
    }
}
