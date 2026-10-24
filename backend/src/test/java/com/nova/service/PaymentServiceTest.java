package com.nova.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nova.auth.Role;
import com.nova.auth.User;
import com.nova.orders.OrderRecord;
import com.nova.payments.PaymentDtos;
import com.nova.payments.PaymentRecord;
import com.nova.payments.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private OrderService orderService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BackgroundJobService backgroundJobService;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(
                orderService,
                paymentRepository,
                "test-key",
                "test-secret",
                "webhook-secret",
                new ObjectMapper(),
                backgroundJobService
        );
    }

    @Test
    void mineReturnsPersistedPaymentsForUser() {
        User user = new User("Alice", "alice@example.com", "pass", Role.CUSTOMER);
        OrderRecord order = new OrderRecord(user, BigDecimal.valueOf(4999), "Alice", "+91 9999999999", "Address", "alice@example.com", "[]");
        order.setId(101L);
        PaymentRecord payment = new PaymentRecord(order, "order_rzp_123");
        payment.setId(202L);
        payment.paid("pay_rzp_456");

        when(paymentRepository.findByOrder_User_EmailOrderByUpdatedAtDesc("alice@example.com"))
                .thenReturn(List.of(payment));

        List<PaymentDtos.PaymentSummary> results = paymentService.mine("alice@example.com");

        assertEquals(1, results.size());
        assertEquals(202L, results.get(0).id());
        assertEquals(101L, results.get(0).orderId());
        assertEquals("order_rzp_123", results.get(0).razorpayOrderId());
        assertEquals("pay_rzp_456", results.get(0).razorpayPaymentId());
        assertEquals("PAID", results.get(0).status());
        assertEquals(BigDecimal.valueOf(4999), results.get(0).amount());
    }
}
