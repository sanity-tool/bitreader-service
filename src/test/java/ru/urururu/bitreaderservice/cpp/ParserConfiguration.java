package ru.urururu.bitreaderservice.cpp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.urururu.bitreaderservice.dto.ModuleDto;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Function;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Configuration
public class ParserConfiguration {
    @Autowired
    NativeBytecodeParser nativeBytecodeParser;

    @Bean
    Function<byte[], ModuleDto> moduleReader() {
        String parserUrl = System.getenv("PARSER_URL");
        if (StringUtils.isNotEmpty(parserUrl)) {
            return bytes -> {
                try {
                    URL url = new URL(parserUrl);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.addRequestProperty("Content-Type", "application/octet-stream");
                    urlConnection.setDoOutput(true);
                    OutputStream os = urlConnection.getOutputStream();
                    os.write(bytes);
                    os.flush();
                    os.close();

                    int responseCode = urlConnection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) { //success
                        ObjectMapper mapper = new ObjectMapper();

                        return mapper.readValue(urlConnection.getInputStream(), ModuleDto.class);
                    } else {
                        throw new IllegalStateException("code was " + responseCode);
                    }
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            };
        }

        return nativeBytecodeParser::parse;
    }
}
