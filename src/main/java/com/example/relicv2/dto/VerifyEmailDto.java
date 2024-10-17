package com.example.relicv2.dto;

import lombok.Data;

@Data
public class VerifyEmailDto {
    private String otp;
    private String verificationToken;
}
