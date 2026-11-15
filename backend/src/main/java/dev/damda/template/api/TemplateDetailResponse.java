package dev.damda.template.api;

import org.jspecify.annotations.Nullable;

public record TemplateDetailResponse(
        Long id, String title, @Nullable String thumbnailUrl) {}
