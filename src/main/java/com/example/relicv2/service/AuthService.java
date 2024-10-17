package com.example.relicv2.service;

import com.example.relicv2.dto.*;

public interface AuthService {
    RegisterResponseDto register(RegisterCustomerDto registerCustomerDto);
    RegisterResponseDto register(RegisterVendorDto registerVendorDto);
    LoginResponseDto login(LoginUserDto loginDto);
    UserDetailsDTO getUserDetails();
    String updateUserDetails(UpdateUserDetailsDto updateUserDetailsDto);
    String verifyEmail(VerifyEmailDto verifyEmailDto);
    RegisterResponseDto resendOtp(String verificationToken);
}
