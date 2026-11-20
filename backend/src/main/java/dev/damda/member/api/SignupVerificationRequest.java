package dev.damda.member.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupVerificationRequest(
        @NotBlank @Email @Size(min = 1, max = 254) String email,
        @NotBlank @Pattern(regexp = "^[0-9]{6}$") String code) {}
