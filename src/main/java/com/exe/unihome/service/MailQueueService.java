package com.exe.unihome.service;

import com.exe.unihome.model.mail.MailJob;

public interface MailQueueService {
  void enqueue(MailJob job);
}
