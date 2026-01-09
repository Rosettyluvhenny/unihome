package com.exe.unihome.mail.service;

import com.exe.unihome.mail.model.MailJob;

public interface MailQueueService {
  void enqueue(MailJob job);
}
