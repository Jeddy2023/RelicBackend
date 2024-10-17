package com.example.relicv2.controller;

import com.example.relicv2.dto.NotificationDto;
import com.example.relicv2.dto.SendNotificationDto;
import com.example.relicv2.service.NotificationService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@AllArgsConstructor
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<String> sendNotification(@RequestBody SendNotificationDto sendNotificationDto){
        notificationService.sendNotification(sendNotificationDto.getMessage(), sendNotificationDto.getUserId());
        return new ResponseEntity<>("Notification sent successfully", HttpStatus.OK);
    }

    @PostMapping("/send-admin")
    public ResponseEntity<String> sendAdminNotification(@RequestBody String message) {
        notificationService.sendAdminNotification(message);
        return new ResponseEntity<>("Admin notification sent successfully", HttpStatus.OK);
    }

    @PatchMapping("/mark-as-read/{notificationId}")
    public ResponseEntity<String> markAsRead(@PathVariable("notificationId") Long notificationId){
        notificationService.markAsRead(notificationId);
        return new ResponseEntity<>("Notification marked as read", HttpStatus.OK);
    }

    @PatchMapping("/mark-all-as-read")
    public ResponseEntity<String> markAllAsRead(){
        notificationService.markAllAsRead();
        return new ResponseEntity<>("All notifications marked as read", HttpStatus.OK);
    }

    @DeleteMapping("/delete/{notificationId}")
    public ResponseEntity<String> deleteNotification(@PathVariable("notificationId") Long notificationId){
        notificationService.deleteNotification(notificationId);
        return new ResponseEntity<>("Notification deleted successfully", HttpStatus.OK);
    }

    @DeleteMapping("/delete-all")
    public ResponseEntity<String> deleteAllNotifications(){
        notificationService.deleteAllNotifications();
        return new ResponseEntity<>("All notifications deleted successfully", HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Page<NotificationDto>> getNotifications(
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "Date Ascending") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<NotificationDto> notifications = notificationService.getNotifications(isRead, startDate, endDate, sortBy);

        int start = Math.min(page * size, notifications.size());
        int end = Math.min((page + 1) * size, notifications.size());
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationDto> notificationPage = new PageImpl<>(notifications.subList(start, end), pageable, notifications.size());

        return new ResponseEntity<>(notificationPage, HttpStatus.OK);
    }

    @GetMapping("/admin")
    public ResponseEntity<Page<NotificationDto>> getAdminNotifications(
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "Date Descending") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<NotificationDto> notifications = notificationService.getAdminNotifications(isRead, startDate, endDate, sortBy, keyword);

        int start = Math.min(page * size, notifications.size());
        int end = Math.min((page + 1) * size, notifications.size());
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationDto> notificationPage = new PageImpl<>(notifications.subList(start, end), pageable, notifications.size());

        return new ResponseEntity<>(notificationPage, HttpStatus.OK);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDto>> getUnreadNotifications(){
        return new ResponseEntity<>(notificationService.getUnreadNotifications(), HttpStatus.OK);
    }
}
