package dev.damda.template.domain;

import lombok.Getter;

@Getter
public enum Format {
    NOTION(DeliveryType.LINK),
    PPT(DeliveryType.FILE);

    private final DeliveryType deliveryType;

    Format(DeliveryType deliveryType) {
        this.deliveryType = deliveryType;
    }
}
