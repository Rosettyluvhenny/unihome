package com.exe.unihome.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
  @Bean
  public OpenAPI customOpenAPI(
    @Value("${openapi.title}") String title,
    @Value("${openapi.version}") String version,
    @Value("${openapi.description}") String description,
    @Value("${openapi.serverUrl}") String serverUrl,
    @Value("${openapi.serverName}") String serverName) {

    return new OpenAPI()
      .info(
        new Info()
          .title("UNIHOME")
          .version("v1.0.0")
          .description(description)
          .license(new License().name("Api License").url(serverUrl)))
      .servers(List.of(new Server().url(serverUrl).description(serverName)))
      .components(
        new Components()
          .addSecuritySchemes(
            "bearerAuth",
            new SecurityScheme()
              .type(SecurityScheme.Type.HTTP)
              .scheme("bearer")
              .bearerFormat("JWT")))
      .security(List.of(new SecurityRequirement().addList("bearerAuth")));
  }
}
