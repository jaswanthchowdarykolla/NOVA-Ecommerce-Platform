package com.nova.payments;

import java.math.BigDecimal;
import java.time.Instant;

public final class PaymentDtos {
    private PaymentDtos() {}

    public record PaymentSummary(
            long id,
            long orderId,
            String razorpayOrderId,
            String razorpayPaymentId,
            String status,
            BigDecimal amount,
            Instant updatedAt,
            Instant orderCreatedAt
    ) {
        public static PaymentSummary from(PaymentRecord p) {
            return new PaymentSummary(
                    p.getId() != null ? p.getId() : 0L,
                    p.getOrder() != null && p.getOrder().getId() != null ? p.getOrder().getId() : 0L,
                    p.getRazorpayOrderId(),
                    p.getRazorpayPaymentId(),
                    p.getStatus(),
                    p.getOrder() != null ? p.getOrder().getTotalAmount() : BigDecimal.ZERO,
                    p.getUpdatedAt(),
                    p.getOrder() != null ? p.getOrder().getCreatedAt() : Instant.now()
            );
        }
    }
}
