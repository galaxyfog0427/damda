package dev.damda.member.api;

import jakarta.validation.constraints.NotBlank;

public record WithdrawalRequest(@NotBlank String password) {}
