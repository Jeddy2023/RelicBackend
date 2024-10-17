package com.example.relicv2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDetailsDto {
    private Long id;
    private String firstname;
    private String lastname;
    private String email;
    private String username;
    private String shopname;
    private String phonenumber;
    private boolean isVerified;
    private boolean isBlocked;
    private String role;
    private Date createdAt;
}
