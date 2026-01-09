package com.exe.unihome.controller;

import com.exe.unihome.common.model.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RedisDebugController {

  private final StringRedisTemplate redisTemplate;

  @GetMapping("/redis-test")
  public ResponseEntity<ApiResponse<String>> test() {
    redisTemplate.opsForValue().set("ping", "pong");
    String result = redisTemplate.opsForValue().get("ping");
    return ResponseEntity.ok(ApiResponse.<String>builder()
      .code(200)
      .message("Redis test successful")
      .data(result)
      .build());
  }
}
