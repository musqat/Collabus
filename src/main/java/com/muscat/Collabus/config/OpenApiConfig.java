package com.muscat.Collabus.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger UI 에 Bearer 토큰 입력을 붙인다.
 * 로그인으로 받은 Access Token 을 한 번 넣으면 이후 요청 헤더에 함께 나간다.
 */
@Configuration
public class OpenApiConfig {

  private static final String SCHEME_NAME = "bearerAuth";

  @Bean
  public OpenAPI collabusOpenApi() {
    SecurityScheme bearer = new SecurityScheme()
        .name(SCHEME_NAME)
        .type(SecurityScheme.Type.HTTP)
        .scheme("bearer")
        .bearerFormat("JWT")
        .description("로그인 응답의 accessToken 을 넣는다. Bearer 접두사는 붙이지 않는다.");

    return new OpenAPI()
        .components(new Components().addSecuritySchemes(SCHEME_NAME, bearer))
        .addSecurityItem(new SecurityRequirement().addList(SCHEME_NAME));
  }
}
