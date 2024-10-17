package com.example.relicv2.service;

public interface MailingService {
    void sendEmail(String to, String subject, String text);
}
