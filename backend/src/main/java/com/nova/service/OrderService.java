package com.nova.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nova.auth.UserRepository;
import com.nova.orders.OrderDtos;
import com.nova.orders.OrderRecord;
import com.nova.orders.OrderRepository;
import com.nova.product.ProductSizes;
import com.nova.product.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {
    private static final BigDecimal FREE_DELIVERY_THRESHOLD = BigDecimal.valueOf(999);
    private static final BigDecimal DELIVERY_FEE = BigDecimal.valueOf(99);
    private final OrderRepository orders;
    private final UserRepository users;
    private final ProductRepository products;
    private final ObjectMapper mapper;

    public OrderService(OrderRepository orders, UserRepository users, ProductRepository products, ObjectMapper mapper) {
        this.orders = orders;
        this.users = users;
        this.products = products;
        this.mapper = mapper;
    }

    @Transactional
    public OrderDtos.Summary create(String email, OrderDtos.Create request) {
        var user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User account not found"));
        BigDecimal subtotal = BigDecimal.ZERO;
        List<Map<String, Object>> lines = new ArrayList<>();
        for (OrderDtos.Item item : request.items()) {
            var product = products.findById(item.productId()).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown product id: " + item.productId()));
            validateSize(product.getName(), product.getAvailableSizes(), item.size());
            BigDecimal unitPrice = product.getPrice();
            subtotal = subtotal.add(unitPrice.multiply(BigDecimal.valueOf(item.quantity())));
            Map<String, Object> line = new LinkedHashMap<>();
            line.put("productId", product.getId());
            line.put("name", product.getName());
            line.put("unitPrice", unitPrice);
            line.put("quantity", item.quantity());
            if (item.size() != null && !item.size().isBlank()) {
                line.put("size", item.size().trim());
            }
            lines.add(line);
        }
        BigDecimal total = subtotal;
        if (subtotal.compareTo(FREE_DELIVERY_THRESHOLD) <= 0) {
            lines.add(Map.of("name", "Delivery fee", "unitPrice", DELIVERY_FEE, "quantity", 1));
            total = total.add(DELIVERY_FEE);
        }
        try {
            String itemsJson = mapper.writeValueAsString(lines);
            OrderRecord saved = orders.save(new OrderRecord(user, total, request.recipientName().trim(),
                    request.recipientPhone().trim(), request.deliveryAddress().trim(),
                    request.contactEmail() == null ? null : request.contactEmail().trim(), itemsJson));
            return OrderDtos.Summary.from(saved);
        } catch (com.fasterxml.jackson.core.JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize order items", exception);
        }
    }

    @Transactional(readOnly = true)
    public List<OrderDtos.Summary> mine(String email) {
        return orders.findByUser_EmailOrderByCreatedAtDesc(email).stream().map(OrderDtos.Summary::from).toList();
    }

    @Transactional(readOnly = true)
    public OrderRecord owned(long id, String email) {
        OrderRecord order = orders.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        if (!order.getUser().getEmail().equalsIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Order does not belong to this account");
        }
        return order;
    }

    @Transactional
    public void markPaid(long id, String email) {
        owned(id, email).setStatus("PAID");
    }

    private void validateSize(String productName, String availableSizes, String selected) {
        boolean required = ProductSizes.requiresSize(availableSizes);
        boolean provided = selected != null && !selected.isBlank();
        if (required && !provided) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Select a size for " + productName);
        }
        if (required && !ProductSizes.isAllowed(availableSizes, selected)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Size " + selected.trim() + " is not available for " + productName);
        }
        if (!required && provided) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, productName + " does not offer size selection");
        }
    }
}
