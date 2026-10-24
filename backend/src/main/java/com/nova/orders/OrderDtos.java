package com.nova.orders;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class OrderDtos {
    private OrderDtos() { }

    public record Item(@NotNull @Positive Long productId, @Min(1) @Max(50) int quantity, @Size(max = 40) String size) { }

    public record Create(
            @NotEmpty List<@Valid Item> items,
            @NotBlank @Size(max = 100) String recipientName,
            @NotBlank @Pattern(regexp = "^\\+?[0-9 ()-]{8,18}$", message = "Enter a valid phone number") String recipientPhone,
            @NotBlank @Size(min = 8, max = 1000) String deliveryAddress,
            @Email @Size(max = 254) String contactEmail) { }

    public record Summary(
            long id,
            BigDecimal totalAmount,
            String status,
            Instant createdAt,
            String recipientName,
            String recipientPhone,
            String deliveryAddress,
            String contactEmail,
            String itemsJson) {
        public static Summary from(OrderRecord order) {
            Long id = order.getId();
            return new Summary(
                    id == null ? 0L : id,
                    order.getTotalAmount(),
                    order.getStatus(),
                    order.getCreatedAt(),
                    order.getRecipientName(),
                    order.getRecipientPhone(),
                    order.getDeliveryAddress(),
                    order.getContactEmail(),
                    order.getItemsJson());
        }
    }
}
