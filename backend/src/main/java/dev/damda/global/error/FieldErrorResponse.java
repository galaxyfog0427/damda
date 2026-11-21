package dev.damda.global.error;

import org.jspecify.annotations.Nullable;

public record FieldErrorResponse(String field, @Nullable String message) {}
