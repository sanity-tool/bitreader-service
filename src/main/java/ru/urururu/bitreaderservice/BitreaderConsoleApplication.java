package ru.urururu.bitreaderservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.urururu.bitreaderservice.cpp.NativeBytecodeParser;
import ru.urururu.bitreaderservice.dto.ModuleDto;

import java.io.File;
import java.nio.file.Files;

@SpringBootApplication
public class BitreaderConsoleApplication implements CommandLineRunner {
    @Autowired
    NativeBytecodeParser parser;

    @Override
    public void run(String... args) throws Exception {
        BitreaderOptions options = new BitreaderOptions();

        try {
            new CmdLineParser(options).parseArgument(args);
        } catch (CmdLineException e) {
            new CmdLineParser(options).printUsage(System.out);
            System.exit(1);
        }

        ModuleDto module = parser.parse(Files.readAllBytes(options.in.toPath()));

        ObjectMapper mapper = new ObjectMapper();

        if (options.out != null) {
            mapper.writeValue(options.out, module);
        } else {
            mapper.writeValue(System.out, module);
        }
    }

    class BitreaderOptions {
        @Option(name = "-in", usage = "input file", required = true)
        public File in;

        @Option(name = "-out", usage = "output file")
        public File out;
    }
}
