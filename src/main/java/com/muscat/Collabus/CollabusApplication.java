package com.muscat.Collabus;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
@OpenAPIDefinition(
		info = @Info(
				title = "Collabus",
				description = "Collabus API",
				version = "v1",
				contact = @Contact(
						name = "Muscat Han",
						email = "muscat@example.com",
						url = "https://github.com/"
				),
				license = @License(
						name = "Apache 2.0",
						url = "https://github.com/"
				)
		)
)
public class CollabusApplication {

	public static void main(String[] args) {
		SpringApplication.run(CollabusApplication.class, args);
	}

}
