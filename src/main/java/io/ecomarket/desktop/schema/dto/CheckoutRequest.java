package io.ecomarket.desktop.schema.dto;

import io.ecomarket.desktop.schema.enums.DeliveryMethod;

public record CheckoutRequest(
        String buyerId,
        DeliveryMethod deliveryMethod,
        String shippingAddress
) {}
