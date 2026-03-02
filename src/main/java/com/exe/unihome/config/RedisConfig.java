package com.exe.unihome.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Redis Configuration
 * Configures Redisson client for distributed data structures and delayed queues
 * Configures RedisTemplate for general Redis operations and caching
 */
@Configuration
@EnableAsync
@EnableScheduling
public class RedisConfig {

  @Value("${spring.data.redis.host}")
  private String redisHost;

  @Value("${spring.data.redis.port}")
  private int redisPort;

  @Value("${spring.data.redis.password:}")
  private String redisPassword;

  /**
   * Redisson Client Bean
   * Used for distributed data structures, delayed queues, and locks
   */
  @Bean
  public RedissonClient redissonClient() {
    Config config = new Config();
    String redisUrl = "redis://" + redisHost + ":" + redisPort;

    config
      .useSingleServer()
      .setAddress(redisUrl)
      .setPassword(redisPassword.isEmpty() ? null : redisPassword)
      .setConnectionMinimumIdleSize(1)
      .setConnectionPoolSize(10)
      .setRetryAttempts(3)
      .setRetryInterval(1500);

    return Redisson.create(config);
  }

  /**
   * RedisTemplate Bean
   * Used for standard cache operations and serialization
   */
  @Bean
  public RedisTemplate<String, Object> redisTemplate(
    RedisConnectionFactory connectionFactory) {

    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);

    // Configure ObjectMapper with Java Time support
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.activateDefaultTyping(
      BasicPolymorphicTypeValidator.builder()
        .allowIfBaseType(Object.class)
        .build(),
      ObjectMapper.DefaultTyping.NON_FINAL,
      JsonTypeInfo.As.PROPERTY
    );

    // Use JSON serializer for values
    GenericJackson2JsonRedisSerializer serializer =
      new GenericJackson2JsonRedisSerializer(objectMapper);

    // Set serializers
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(serializer);
    template.setHashKeySerializer(new StringRedisSerializer());
    template.setHashValueSerializer(serializer);

    template.afterPropertiesSet();
    return template;
  }
}

