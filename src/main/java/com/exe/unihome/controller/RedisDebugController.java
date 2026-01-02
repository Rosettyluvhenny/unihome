package com.exe.unihome.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RedisDebugController {

  private final StringRedisTemplate redisTemplate;

  @GetMapping("/redis-test")
  public String test() {
    redisTemplate.opsForValue().set("ping", "pong");
    return redisTemplate.opsForValue().get("ping");
  }
}
