package com.nova.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nova.auth.Role;
import com.nova.auth.User;
import com.nova.auth.UserRepository;
import com.nova.orders.OrderDtos;
import com.nova.orders.OrderRecord;
import com.nova.orders.OrderRepository;
import com.nova.product.Product;
import com.nova.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock OrderRepository orders;
    @Mock UserRepository users;
    @Mock ProductRepository products;
    @Spy ObjectMapper mapper = new ObjectMapper();
    @InjectMocks OrderService service;

    private User user;
    private Product sneakers;
    private Product lamp;

    @BeforeEach
    void setUp() {
        user = new User("Ada Lovelace", "a@b.com", "secret", Role.CUSTOMER);
        sneakers = new Product(251L, "Urban Runner Sneakers", "footwear-251", "Footwear", "Northline",
                new BigDecimal("4999.00"), 4.5, 20, "Runners", "https://images.example.test/s.jpg",
                "Obsidian", false, "footwear", "UK 7,UK 8,UK 9");
        lamp = new Product(376L, "Sculptural Table Lamp", "home-376", "Home & Living", "NOVA Select",
                new BigDecimal("3999.00"), 4.5, 10, "Lamp", "https://images.example.test/l.jpg",
                "Stone", false, "lamp", null);
    }

    @Test
    void rejectsSizedProductWithoutSize() {
        when(users.findByEmailIgnoreCase("a@b.com")).thenReturn(Optional.of(user));
        when(products.findById(251L)).thenReturn(Optional.of(sneakers));
        OrderDtos.Create request = new OrderDtos.Create(
                List.of(new OrderDtos.Item(251L, 1, null)),
                "Ada Lovelace", "+91 9876543210", "12 Church Street, Bengaluru 560001", "a@b.com");
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.create("a@b.com", request));
        assertTrue(ex.getReason().contains("Select a size"));
    }

    @Test
    void rejectsUnavailableSize() {
        when(users.findByEmailIgnoreCase("a@b.com")).thenReturn(Optional.of(user));
        when(products.findById(251L)).thenReturn(Optional.of(sneakers));
        OrderDtos.Create request = new OrderDtos.Create(
                List.of(new OrderDtos.Item(251L, 1, "UK 5")),
                "Ada Lovelace", "+91 9876543210", "12 Church Street, Bengaluru 560001", "a@b.com");
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.create("a@b.com", request));
        assertTrue(ex.getReason().contains("not available"));
    }

    @Test
    void persistsDistinctSizesAndIgnoresSizeOnNonVariantProducts() throws Exception {
        when(users.findByEmailIgnoreCase("a@b.com")).thenReturn(Optional.of(user));
        when(products.findById(251L)).thenReturn(Optional.of(sneakers));
        when(products.findById(376L)).thenReturn(Optional.of(lamp));
        when(orders.save(any(OrderRecord.class))).thenAnswer(call -> call.getArgument(0));

        OrderDtos.Create request = new OrderDtos.Create(
                List.of(
                        new OrderDtos.Item(251L, 1, "UK 8"),
                        new OrderDtos.Item(251L, 2, "UK 9"),
                        new OrderDtos.Item(376L, 1, null)
                ),
                "Ada Lovelace", "+91 9876543210", "12 Church Street, Bengaluru 560001", "a@b.com");
        service.create("a@b.com", request);

        ArgumentCaptor<OrderRecord> captor = ArgumentCaptor.forClass(OrderRecord.class);
        verify(orders).save(captor.capture());
        String json = captor.getValue().getItemsJson();
        assertTrue(json.contains("\"size\":\"UK 8\""));
        assertTrue(json.contains("\"size\":\"UK 9\""));
        assertTrue(json.contains("Sculptural Table Lamp"));
        assertFalse(json.matches("(?s).*376[^\\[]*\"size\".*"));
    }
}
