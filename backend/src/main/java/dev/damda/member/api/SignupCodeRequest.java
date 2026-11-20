package dev.damda.member.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupCodeRequest(
        @NotBlank @Email @Size(min = 1, max = 254) String email) {}
