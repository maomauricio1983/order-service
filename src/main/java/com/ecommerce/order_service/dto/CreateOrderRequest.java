package com.ecommerce.order_service.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record CreateOrderRequest(
        @NotBlank(message = "customerId es requerido")
        String customerId,

        @NotEmpty(message = "La orden debe tener al menos un ítem")
        @Valid
        List<OrderItemRequest> items
) {}