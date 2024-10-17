package com.example.relicv2.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterCustomerDto {
    @NotNull(message = "Firstname must be provided")
    @NotBlank(message = "Firstname cannot be empty")
    private String firstname;

    @NotNull(message = "Lastname must be provided")
    @NotBlank(message = "Lastname cannot be empty")
    private String lastname;

    @NotNull(message = "Email must be provided")
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email must be valid")
    private String email;

    @NotNull(message = "Password must be provided")
    @NotBlank(message = "Password cannot be empty")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[\\W_]).{5,16}$", message = "Password must be between 5 to 16 characters, contain at least one uppercase letter, and one symbol")
    private String password;

    @NotNull(message = "Role must be provided")
    @NotBlank(message = "Role cannot be empty")
    private String role;

    @NotNull(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number should be between 10 to 15 digits")
    private String phonenumber;
}
