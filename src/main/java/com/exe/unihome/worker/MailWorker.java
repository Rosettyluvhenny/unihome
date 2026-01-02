package com.exe.unihome.worker;

import com.exe.unihome.model.mail.MailJob;
import com.exe.unihome.service.MailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Slf4j
public class MailWorker {

  private static final String MAIL_QUEUE_KEY = "mail:queue";

  private static final String MAIL_PROCESSING_KEY = "mail:processing";
  private static final int MAX_RETRY = 3;

  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;
  private final MailService mailService;
  private final TaskExecutor mailWorkerExecutor;
  private volatile boolean running = true;

  public MailWorker(
    StringRedisTemplate redisTemplate,
    ObjectMapper objectMapper,
    MailService mailService,
    @Qualifier("mailWorkerExecutor") TaskExecutor mailWorkerExecutor
  ) {
    this.redisTemplate = redisTemplate;
    this.objectMapper = objectMapper;
    this.mailService = mailService;
    this.mailWorkerExecutor = mailWorkerExecutor;
  }

  @PostConstruct
  public void init() {
    recoverStuckJobs();
    mailWorkerExecutor.execute(this::run);
  }

  @Scheduled(fixedDelay = 60_000)
  public void scheduledRecovery() {
    recoverStuckJobs();
  }

  @PreDestroy
  public void shutdown() {
    log.info("[MailWorker] shutting down");
    running = false;
  }

  private void run() {
    log.info("[MailWorker] started");

    while (running) {
      try {
        String payload = redisTemplate.opsForList()
          .rightPopAndLeftPush(
            MAIL_QUEUE_KEY,
            MAIL_PROCESSING_KEY,
            Duration.ofSeconds(3)
          );

        if (payload == null) {
          continue;
        }

        MailJob job = objectMapper.readValue(payload, MailJob.class);
        process(job, payload);

      } catch (RedisConnectionFailureException e) {
        log.warn("[MailWorker] redis unavailable, stopping worker");
        break; // ❗ rất quan trọng
      } catch (Exception e) {
        log.error("[MailWorker] unexpected error", e);
      }
    }

    log.info("[MailWorker] stopped");
  }

  private void process(MailJob job, String payload) {
    try {
      log.info("[MailWorker] processing job {}", job.getJobId());

      if ("VERIFY_EMAIL".equals(job.getType())) {
        mailService.sendVerificationEmail(
          job.getToEmail(),
          job.getFullName(),
          job.getVerifyToken()
        );
      }
      ack(payload);
      log.info("[MailWorker] job {} completed", job.getJobId());

    } catch (Exception e) {
      handleRetry(job, payload, e);
    }
  }

  private void handleRetry(MailJob job, String payload, Exception e) {
    int retry = job.getRetryCount() + 1;

    ack(payload);
    if (retry <= MAX_RETRY) {
      job.setRetryCount(retry);
      log.warn("[MailWorker] retry {} for job {}", retry, job.getJobId());

      try {
        String newPayload = objectMapper.writeValueAsString(job);
        redisTemplate.opsForList().leftPush(MAIL_QUEUE_KEY, newPayload);
      } catch (Exception ex) {
        log.error("[MailWorker] failed to requeue job {}", job.getJobId(), ex);
      }

    } else {
      log.error("[MailWorker] job {} failed after max retries", job.getJobId(), e);
      // TODO: move to dead-letter queue
    }
  }

  private void ack(String payload) {
    redisTemplate.opsForList()
      .remove(MAIL_PROCESSING_KEY, 1, payload);
  }

  private void recoverStuckJobs() {
    log.info("[MailWorker] recovering stuck jobs");

    while (true) {
      String payload = redisTemplate.opsForList()
        .rightPopAndLeftPush(
          MAIL_PROCESSING_KEY,
          MAIL_QUEUE_KEY
        );

      if (payload == null) {
        break;
      }

      log.warn("[MailWorker] recovered stuck job");
    }
  }
}
