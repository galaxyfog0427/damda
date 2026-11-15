package dev.damda.template.api;

import dev.damda.template.domain.AccessType;
import dev.damda.template.domain.Category;
import dev.damda.template.domain.Format;
import org.jspecify.annotations.Nullable;

public record TemplateSummaryResponse(
        Long id,
        String title,
        @Nullable String thumbnailUrl,
        Format format,
        Category category,
        AccessType accessType,
        long downloadCount,
        TemplateCreatorResponse creator) {}
