package com.nova.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nova.orders.OrderRecord;
import com.nova.payments.PaymentRecord;
import com.nova.payments.PaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@Service
public class PaymentService {
    private final OrderService orders;
    private final PaymentRepository payments;
    private final String keyId;
    private final String keySecret;
    private final String webhookSecret;
    private final ObjectMapper mapper;
    private final BackgroundJobService backgroundJobs;
    private final RestClient razorpay = RestClient.builder().baseUrl("https://api.razorpay.com/v1").build();

    public PaymentService(OrderService orders, PaymentRepository payments,
            @Value("${razorpay.key.id:}") String keyId,
            @Value("${razorpay.key.secret:}") String keySecret,
            @Value("${razorpay.webhook.secret:}") String webhookSecret,
            ObjectMapper mapper, BackgroundJobService backgroundJobs) {
        this.orders = orders;
        this.payments = payments;
        this.keyId = keyId == null ? "" : keyId;
        this.keySecret = keySecret == null ? "" : keySecret;
        this.webhookSecret = webhookSecret == null ? "" : webhookSecret;
        this.mapper = mapper;
        this.backgroundJobs = backgroundJobs;
    }

    @Transactional
    public Map<String, Object> create(String email, long orderId) {
        requireRazorpayKeys();
        OrderRecord order = orders.owned(orderId, email);
        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order is not awaiting payment");
        }
        long amountPaise = order.getTotalAmount().movePointRight(2).longValueExact();
        // Reuse a previously created gateway order if the customer closed the checkout popup.
        var existing = payments.findByOrder_Id(orderId);
        if (existing.isPresent()) {
            PaymentRecord payment = existing.get();
            return paymentOrderResponse(payment.getRazorpayOrderId(), amountPaise, orderId);
        }
        try {
            Map<?, ?> body = razorpay.post().uri("/orders")
                    .headers(headers -> headers.setBasicAuth(keyId, keySecret))
                    .body(Map.of("amount", amountPaise, "currency", "INR", "receipt", "nova-order-" + orderId))
                    .retrieve().body(Map.class);
            if (body == null || body.get("id") == null) {
                throw new IllegalStateException("Razorpay did not return an order id");
            }
            String gatewayOrderId = String.valueOf(body.get("id"));
            payments.save(new PaymentRecord(order, gatewayOrderId));
            return paymentOrderResponse(gatewayOrderId, amountPaise, orderId);
        } catch (RestClientResponseException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Razorpay order creation failed with status " + exception.getStatusCode());
        }
    }

    @Transactional
    public Map<String, Object> verify(String email, long orderId, String razorpayOrderId,
            String paymentId, String signature) {
        requireRazorpayKeys();
        OrderRecord order = orders.owned(orderId, email);
        PaymentRecord payment = payments.findByOrder_Id(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment order not found"));
        if (!payment.getRazorpayOrderId().equals(razorpayOrderId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment order mismatch");
        }
        String expected = hmac(razorpayOrderId + "|" + paymentId, keySecret);
        if (!constantTimeEquals(expected, signature)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment signature is invalid");
        }
        boolean alreadyPaid = "PAID".equals(order.getStatus());
        payment.paid(paymentId);
        orders.markPaid(order.getId(), email);
        if (!alreadyPaid) backgroundJobs.startOrderJobs(order.getId());
        return Map.of("status", "PAID", "orderId", orderId, "paymentId", paymentId);
    }

    @Transactional
    public Map<String, Object> webhook(String rawBody, String signature) {
        if (webhookSecret.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Razorpay webhook secret is not configured");
        }
        if (!constantTimeEquals(hmac(rawBody == null ? "" : rawBody, webhookSecret), signature)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid webhook signature");
        }
        try {
            JsonNode root = mapper.readTree(rawBody);
            String event = root.path("event").asText();
            if (!"payment.captured".equals(event)) {
                return Map.of("received", true, "processed", false, "event", event);
            }
            JsonNode entity = root.path("payload").path("payment").path("entity");
            String gatewayOrderId = entity.path("order_id").asText("");
            String paymentId = entity.path("id").asText("");
            if (gatewayOrderId.isBlank() || paymentId.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Webhook payment data is incomplete");
            }
            PaymentRecord payment = payments.findByRazorpayOrderId(gatewayOrderId).orElse(null);
            if (payment == null) {
                return Map.of("received", true, "processed", false, "reason", "No matching NOVA order");
            }
            long expectedAmountPaise = payment.getOrder().getTotalAmount().movePointRight(2).longValueExact();
            long capturedAmountPaise = entity.path("amount").asLong(-1);
            String currency = entity.path("currency").asText("");
            if (capturedAmountPaise != expectedAmountPaise || !"INR".equalsIgnoreCase(currency)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Webhook amount or currency does not match the order");
            }
            boolean alreadyPaid = "PAID".equals(payment.getOrder().getStatus());
            payment.paid(paymentId);
            payment.getOrder().setStatus("PAID");
            if (!alreadyPaid) backgroundJobs.startOrderJobs(payment.getOrder().getId());
            return Map.of("received", true, "processed", true, "orderId", payment.getOrder().getId());
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid webhook payload");
        }
    }

    @Transactional(readOnly = true)
    public List<com.nova.payments.PaymentDtos.PaymentSummary> mine(String email) {
        return payments.findByOrder_User_EmailOrderByUpdatedAtDesc(email)
                .stream()
                .map(com.nova.payments.PaymentDtos.PaymentSummary::from)
                .toList();
    }

    private Map<String, Object> paymentOrderResponse(String gatewayOrderId, long amountPaise, long localOrderId) {
        return Map.of("keyId", keyId, "razorpayOrderId", gatewayOrderId,
                "amount", amountPaise, "currency", "INR", "localOrderId", localOrderId);
    }

    private void requireRazorpayKeys() {
        if (keyId.isBlank() || keySecret.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Razorpay test keys are not configured");
        }
    }

    private String hmac(String value, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to verify payment signature", exception);
        }
    }

    private boolean constantTimeEquals(String expected, String supplied) {
        if (expected == null || supplied == null) return false;
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8));
    }
}
