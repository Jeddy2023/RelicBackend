package com.example.relicv2.repository;

import com.example.relicv2.enums.OtpType;
import com.example.relicv2.model.Otp;
import com.example.relicv2.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp, Long> {
    void deleteAllByUser(User user);

    void deleteAllByUserAndOtpType(User user, OtpType otpType);
    Optional<Otp> findByUserAndTokenAndOtpType(User user, String token, OtpType otpType);
}
