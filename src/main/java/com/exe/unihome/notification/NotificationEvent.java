package com.exe.unihome.notification;

public record NotificationEvent(String type,
                                String userId,
                                String title,
                                String content) {
}
