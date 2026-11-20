package dev.damda.member.api;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank String signupToken,
        @NotBlank @Size(min = 8, max = 20) String password,
        @NotBlank @Size(min = 2, max = 20) String nickname,
        @AssertTrue boolean termsAgreed,
        @AssertTrue boolean over14) {}
