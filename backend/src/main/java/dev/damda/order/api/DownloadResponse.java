package dev.damda.order.api;

import dev.damda.template.domain.DeliveryType;
import java.time.Instant;
import org.jspecify.annotations.Nullable;

public record DownloadResponse(
        DeliveryType deliveryType, String url, @Nullable Instant expiresAt, boolean addedToLibrary) {}
