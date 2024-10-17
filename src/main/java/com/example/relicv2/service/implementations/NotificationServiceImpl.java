package com.example.relicv2.service.implementations;

import com.example.relicv2.dto.NotificationDto;
import com.example.relicv2.enums.Role;
import com.example.relicv2.exception.ResourceNotFoundException;
import com.example.relicv2.model.Notification;
import com.example.relicv2.model.User;
import com.example.relicv2.repository.NotificationRepository;
import com.example.relicv2.repository.UserRepository;
import com.example.relicv2.service.NotificationService;
import com.example.relicv2.util.UserAuthenticationHelper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private UserAuthenticationHelper userAuthenticationHelper;
    private final ModelMapper modelMapper;

    @Override
    public void sendNotification(String message, Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("User not found");
        }

        User user = userOptional.get();

        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setUser(user);
        notificationRepository.save(notification);
    }

    @Override
    public void sendAdminNotification(String message) {
        List<User> admins = userRepository.findByRole(Role.ADMIN);
        if (admins.isEmpty()) {
            throw new ResourceNotFoundException("No admins found");
        }

        for (User admin : admins) {
            Notification notification = new Notification();
            notification.setMessage(message);
            notification.setUser(admin);
            notification.setAdminNotification(true);
            notificationRepository.save(notification);
        }
    }


    @Override
    public void markAsRead(Long notificationId) {
        Optional<Notification> notificationOptional = notificationRepository.findById(notificationId);
        if (notificationOptional.isEmpty()) {
            throw new ResourceNotFoundException("Notification not found");
        }

        Notification notification = notificationOptional.get();
        notification.setRead(true);
    }

    @Override
    public void markAllAsRead() {
        Optional<User> userOptional = userRepository.findById(userAuthenticationHelper.getLoggedInUser().getId());
        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("User not found");
        }

        User user = userOptional.get();
        List<Notification> userNotifications = user.getNotifications();
        if (userNotifications.isEmpty()) {
            return;
        }
        userNotifications.forEach(notification -> notification.setRead(true));
        userRepository.save(user);
    }

    @Override
    public void deleteNotification(Long notificationId) {
        Optional<Notification> notificationOptional = notificationRepository.findById(notificationId);
        if (notificationOptional.isEmpty()) {
            throw new ResourceNotFoundException("Notification not found");
        }

        Notification notification = notificationOptional.get();
        notificationRepository.delete(notification);
    }

    @Override
    public void deleteAllNotifications() {
        Optional<User> userOptional = userRepository.findById(userAuthenticationHelper.getLoggedInUser().getId());
        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("User not found");
        }

        User user = userOptional.get();
        user.getNotifications().forEach(notificationRepository::delete);
    }

    @Override
    public List<NotificationDto> getNotifications(
            Boolean isRead,
            String startDateStr,
            String endDateStr,
            String sortBy
    ) {
        User user = userAuthenticationHelper.getLoggedInUser();
        List<Notification> notifications = notificationRepository.findByUser(user);

        if (isRead != null) {
            notifications = notifications.stream()
                    .filter(notification -> notification.isRead() == isRead)
                    .collect(Collectors.toList());
        }

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS", Locale.ENGLISH);
            if (startDateStr != null && endDateStr != null) {
                Date startDate = dateFormat.parse(startDateStr);
                Date endDate = dateFormat.parse(endDateStr);
                notifications = notifications.stream()
                        .filter(notification -> notification.getTimestamp().after(startDate) && notification.getTimestamp().before(endDate))
                        .collect(Collectors.toList());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        switch (sortBy) {
            case "Date Ascending":
                notifications.sort(Comparator.comparing(Notification::getTimestamp));
                break;
            case "Date Descending":
                notifications.sort(Comparator.comparing(Notification::getTimestamp).reversed());
                break;
        }

        return notifications.stream()
                .map(notification -> modelMapper.map(notification, NotificationDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationDto> getAdminNotifications(
            Boolean isRead,
            String startDateStr,
            String endDateStr,
            String sortBy,
            String keyword
    ) {
        List<Notification> notifications = notificationRepository.findByIsAdminNotificationTrue();

        if (isRead != null) {
            notifications = notifications.stream()
                    .filter(notification -> notification.isRead() == isRead)
                    .collect(Collectors.toList());
        }

        if (startDateStr != null && endDateStr != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS", Locale.ENGLISH);
            try {
                Date startDate = dateFormat.parse(startDateStr);
                Date endDate = dateFormat.parse(endDateStr);
                notifications = notifications.stream()
                        .filter(notification -> notification.getTimestamp().after(startDate) && notification.getTimestamp().before(endDate))
                        .collect(Collectors.toList());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            notifications = notifications.stream()
                    .filter(notification -> notification.getMessage().toLowerCase().contains(keyword.toLowerCase()))
                    .collect(Collectors.toList());
        }

        switch (sortBy) {
            case "Date Ascending":
                notifications.sort(Comparator.comparing(Notification::getTimestamp));
                break;
            case "Date Descending":
                notifications.sort(Comparator.comparing(Notification::getTimestamp).reversed());
                break;
        }

        return notifications.stream()
                .map(notification -> modelMapper.map(notification, NotificationDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationDto> getUnreadNotifications() {
        Optional<User> userOptional = userRepository.findById(userAuthenticationHelper.getLoggedInUser().getId());
        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("User not found");
        }

        List<Notification> notifications = notificationRepository.findByUserAndIsReadFalse(userOptional.get());
        return notifications.stream().map(notification -> modelMapper.map(notification, NotificationDto.class)).toList();
    }
}
