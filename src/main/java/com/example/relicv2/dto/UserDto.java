package com.example.relicv2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String firstname;
    private String username;
    private String email;
    private String role;
    private boolean isBlocked;
    private boolean isVerified;
    private String shopname;
}
