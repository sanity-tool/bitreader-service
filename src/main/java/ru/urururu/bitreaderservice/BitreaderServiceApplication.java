package ru.urururu.bitreaderservice;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import ru.urururu.bitreaderservice.cpp.NativeBytecodeParser;
import ru.urururu.bitreaderservice.dto.ModuleDto;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.io.File;
import java.io.IOException;

@SpringBootApplication
@RestController
@EnableSwagger2
@EnableWebMvc
public class BitreaderServiceApplication {

	@Autowired
	NativeBytecodeParser parser;

	@RequestMapping(value = "/parse")
	public ModuleDto parse(byte[] bitcode) throws IOException {
		return parser.parse(File.createTempFile(null, null));
	}

	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2).select()
				.apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
				.paths(PathSelectors.any()).build().pathMapping("/")
				.apiInfo(apiInfo()).useDefaultResponseMessages(false);
	}

	@Bean
	ApiInfo apiInfo() {
		final ApiInfoBuilder builder = new ApiInfoBuilder();
		builder.title("sanity-tool bitreader service API").version("1.0").license("(C) Copyright Dmitry Matveev")
				.description("List of all endpoints used in API");
		return builder.build();
	}

	public static void main(String[] args) {
		SpringApplication.run(BitreaderServiceApplication.class, args);
	}
}
