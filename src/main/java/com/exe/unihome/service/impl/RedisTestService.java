package com.exe.unihome.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisTestService {

  private final StringRedisTemplate redisTemplate;

  public void test() {
    redisTemplate.opsForValue().set("test", "ok");
    System.out.println(redisTemplate.opsForValue().get("test"));
  }
}
