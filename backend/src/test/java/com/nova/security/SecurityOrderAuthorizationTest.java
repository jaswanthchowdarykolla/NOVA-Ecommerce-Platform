package com.nova.security;

import com.nova.auth.Role;
import com.nova.auth.User;
import com.nova.auth.UserRepository;
import com.nova.orders.OrderRecord;
import com.nova.orders.OrderRepository;
import com.nova.product.Product;
import com.nova.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityOrderAuthorizationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository users;

    @Autowired
    private ProductRepository products;

    @Autowired
    private OrderRepository orders;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        orders.deleteAll();
        products.deleteAll();
        users.deleteAll();

        User alice = users.save(new User("Alice", "alice@example.com", passwordEncoder.encode("Password123!"), Role.CUSTOMER));
        User bob = users.save(new User("Bob", "bob@example.com", passwordEncoder.encode("Password123!"), Role.CUSTOMER));
        users.save(new User("Admin", "admin@example.com", passwordEncoder.encode("Password123!"), Role.ADMIN));

        products.save(new Product(
                1001L,
                "Drift Earbuds",
                "drift-earbuds",
                "Electronics",
                "NOVA",
                new BigDecimal("2999.00"),
                4.8,
                27,
                "Wireless noise-canceling earbuds",
                "https://example.com/earbuds.png",
                "Black",
                false,
                "electronics"
        ));

        String aliceToken = jwtService.generate(alice);
        String bobToken = jwtService.generate(bob);
        System.setProperty("nova.test.alice.token", aliceToken);
        System.setProperty("nova.test.bob.token", bobToken);
    }

    @Test
    void unauthenticatedCreateOrderRejected() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productId\":1001,\"quantity\":1}],\"recipientName\":\"Alice\",\"recipientPhone\":\"+919999999999\",\"deliveryAddress\":\"12 Main Street\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedCustomerCanCreateOrder() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + System.getProperty("nova.test.alice.token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productId\":1001,\"quantity\":1}],\"recipientName\":\"Alice\",\"recipientPhone\":\"+919999999999\",\"deliveryAddress\":\"12 Main Street\",\"contactEmail\":\"alice@example.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.totalAmount").value(2999.00));
    }

    @Test
    void customerCannotAccessAnotherCustomersOrder() throws Exception {
        User alice = users.findByEmailIgnoreCase("alice@example.com").orElseThrow();
        OrderRecord order = orders.save(new OrderRecord(
                alice,
                new BigDecimal("2999.00"),
                "Alice",
                "+919999999999",
                "12 Main Street",
                "alice@example.com",
                "[{\"productId\":1001,\"name\":\"Drift Earbuds\",\"unitPrice\":2999.00,\"quantity\":1}]"
        ));

        mockMvc.perform(get("/api/orders/{id}", order.getId())
                        .header("Authorization", "Bearer " + System.getProperty("nova.test.bob.token")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminOnlyOperationRejectedForCustomer() throws Exception {
        mockMvc.perform(get("/api/jobs")
                        .header("Authorization", "Bearer " + System.getProperty("nova.test.alice.token")))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidOrderInputRejected() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + System.getProperty("nova.test.alice.token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[],\"recipientName\":\"\",\"recipientPhone\":\"bad\",\"deliveryAddress\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidJwtRejected() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer not-a-valid-jwt"))
                .andExpect(status().isUnauthorized());
    }
}
