package com.exe.unihome.mail.service.impl;

import com.exe.unihome.common.exception.AppException;
import com.exe.unihome.common.exception.ErrorCode;
import com.exe.unihome.mail.model.MailJob;
import com.exe.unihome.mail.service.MailQueueService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailQueueServiceImpl implements MailQueueService {

  private static final String MAIL_QUEUE_KEY = "mail:queue";

  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;

  public void enqueue(MailJob job) {
    try {
      String payload = objectMapper.writeValueAsString(job);
      redisTemplate.opsForList().leftPush(MAIL_QUEUE_KEY, payload);
    } catch (JsonProcessingException e) {
      throw new AppException(ErrorCode.FAIL_MAIL_ENQUEUED);
    }
  }
}
