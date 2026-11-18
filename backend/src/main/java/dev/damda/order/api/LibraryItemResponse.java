package dev.damda.order.api;

import dev.damda.template.domain.Format;
import org.jspecify.annotations.Nullable;

public record LibraryItemResponse(
        Long templateId, String title, @Nullable String thumbnailUrl, Format format) {}
