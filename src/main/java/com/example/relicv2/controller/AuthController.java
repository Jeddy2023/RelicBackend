package com.example.relicv2.controller;

import com.example.relicv2.dto.*;
import com.example.relicv2.service.AuthService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
@CrossOrigin(origins = "*")

public class AuthController {
    private AuthService authService;

    @PostMapping("/register/customer")
    public ResponseEntity<RegisterResponseDto> registerCustomer(@Valid @RequestBody RegisterCustomerDto registerCustomerDto) {
        RegisterResponseDto message = authService.register(registerCustomerDto);
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    @PostMapping("/register/vendor")
    public ResponseEntity<RegisterResponseDto> registerVendor(@Valid @RequestBody RegisterVendorDto registerVendorDto) {
        RegisterResponseDto message = authService.register(registerVendorDto);
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> userLogin(@Valid @RequestBody LoginUserDto loginUserDto){
        return new ResponseEntity<>(authService.login(loginUserDto), HttpStatus.OK);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@Valid @RequestBody VerifyEmailDto verifyEmailDto) {
        String response = authService.verifyEmail(verifyEmailDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<RegisterResponseDto> resendOtp(@RequestBody String verificationToken) {
        RegisterResponseDto response = authService.resendOtp(verificationToken);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<UserDetailsDTO> getUserDetails(){
        UserDetailsDTO user = authService.getUserDetails();
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PatchMapping("/update")
    public ResponseEntity<String> updateUserDetails(@Valid @RequestBody UpdateUserDetailsDto updateUserDetailsDto){
        String updatedUser = authService.updateUserDetails(updateUserDetailsDto);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }
}
