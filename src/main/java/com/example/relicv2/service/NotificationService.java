package com.example.relicv2.service;


import com.example.relicv2.dto.NotificationDto;

import java.util.List;

public interface NotificationService {
    void sendNotification(String message, Long userId);
    void markAsRead(Long notificationId);
    void markAllAsRead();
    void deleteNotification(Long notificationId);
    void deleteAllNotifications();
    List<NotificationDto> getNotifications(
            Boolean isRead,
            String startDateStr,
            String endDateStr,
            String sortBy
    );
    List<NotificationDto> getUnreadNotifications();
    void sendAdminNotification(String message);
    List<NotificationDto> getAdminNotifications(
            Boolean isRead,
            String startDateStr,
            String endDateStr,
            String sortBy,
            String keyword);
}
