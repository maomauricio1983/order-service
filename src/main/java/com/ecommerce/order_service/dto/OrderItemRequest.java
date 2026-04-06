package com.ecommerce.order_service.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(
        @NotBlank(message = "productId es requerido")
        String productId,

        @NotBlank(message = "productName es requerido")
        String productName,

        @NotNull @Min(value = 1, message = "quantity debe ser al menos 1")
        Integer quantity,

        @NotNull @DecimalMin(value = "0.01", message = "price debe ser mayor a 0")
        BigDecimal price
) {}