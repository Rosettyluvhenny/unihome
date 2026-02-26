package com.exe.unihome.config;

import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.stereotype.Component;

import java.time.Clock;

@Component
@EnableJpaAuditing
public class JpaConfig {

  @Bean
  public Clock clock() {
    return Clock.systemDefaultZone();
  }
}
