package com.example.relicv2.service;

import com.example.relicv2.enums.OtpType;
import com.example.relicv2.model.User;

public interface OtpService {

    String create(User user);
    String create(User user, OtpType otpType);
    Boolean verify(User user, String token, OtpType otpType);
    void clearOtps(User user,OtpType otpType);
}
