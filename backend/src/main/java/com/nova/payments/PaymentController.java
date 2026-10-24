package com.nova.payments;

import com.nova.service.PaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@Validated
public class PaymentController {
    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    public record VerifyRequest(
            long orderId,
            @NotBlank String razorpayOrderId,
            @NotBlank String razorpayPaymentId,
            @NotBlank String razorpaySignature
    ) {}

    @GetMapping("/mine")
    public List<PaymentDtos.PaymentSummary> mine(Authentication auth) {
        return service.mine(auth.getName());
    }

    @PostMapping("/create-order")
    public Map<String, Object> create(Authentication auth, @RequestParam long orderId) {
        return service.create(auth.getName(), orderId);
    }

    @PostMapping("/verify")
    public Map<String, Object> verify(Authentication auth, @Valid @RequestBody VerifyRequest req) {
        return service.verify(auth.getName(), req.orderId(), req.razorpayOrderId(), req.razorpayPaymentId(), req.razorpaySignature());
    }

    @PostMapping("/webhook")
    public Map<String, Object> webhook(@RequestBody String rawBody, @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {
        return service.webhook(rawBody, signature);
    }
}
