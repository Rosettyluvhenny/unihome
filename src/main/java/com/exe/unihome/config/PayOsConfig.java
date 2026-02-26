package com.exe.unihome.config;

import io.micrometer.common.lang.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import vn.payos.PayOS;

@Configuration
public class PayOsConfig implements WebMvcConfigurer {

  @Value("${payos.clientId}")
  private String clientId;

  @Value("${payos.apiKey}")
  private String apiKey;

  @Value("${payos.checksumKey}")
  private String checksumKey;

  @Override
  public void addCorsMappings(@NonNull CorsRegistry registry) {
    registry.addMapping("/**")
      .allowedOrigins("*")
      .allowedMethods("*")
      .allowedHeaders("*")
      .exposedHeaders("*")
      .allowCredentials(false)
      .maxAge(3600); // Max age of the CORS pre-flight request
  }

  @Bean
  public PayOS payOS() {
    return new PayOS(clientId, apiKey, checksumKey);
  }
}
