package com.nova.service;

import com.nova.product.Product;
import com.nova.product.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock ProductRepository repository;
    @InjectMocks ProductService service;

    @Test
    void createAssignsIdAndNormalizedSlug() {
        when(repository.findMaxId()).thenReturn(1000L);
        when(repository.save(any(Product.class))).thenAnswer(call -> call.getArgument(0));
        ProductService.ProductInput input = new ProductService.ProductInput(null, "  Studio Lamp ", null,
                "Home & Living", "NOVA Select", new BigDecimal("1299.00"), 4.7, 22,
                "A sculptural lamp", "https://images.example.test/lamp.jpg", "Stone", true, "lamp,home", null);

        Product saved = service.save(input);

        assertEquals(1001L, saved.getId());
        assertEquals("studio-lamp-1001", saved.getSlug());
        assertEquals("Studio Lamp", saved.getName());
        verify(repository).save(any(Product.class));
    }

    @Test
    void createRejectsNegativePrice() {
        ProductService.ProductInput input = new ProductService.ProductInput(null, "Lamp", null,
                "Home", "NOVA", new BigDecimal("-1.00"), 4.0, 0,
                "Description", "https://images.example.test/lamp.jpg", "Black", false, "lamp", null);
        assertThrows(ResponseStatusException.class, () -> service.save(input));
        verify(repository, never()).save(any(Product.class));
    }

    @Test
    void updatePreservesOmittedFields() {
        Product old = new Product(9L, "Old name", "old-name-9", "Home", "NOVA",
                new BigDecimal("500.00"), 4.4, 10, "Old description", "https://images.example.test/a.jpg",
                "Ivory", true, "lamp,desk");
        when(repository.findById(9L)).thenReturn(java.util.Optional.of(old));
        when(repository.save(any(Product.class))).thenAnswer(call -> call.getArgument(0));
        Product updated = service.update(9L, new ProductService.ProductInput(9L, "New name", null,
                null, null, null, null, null, null, null, null, null, null, null));
        assertEquals("New name", updated.getName());
        assertEquals("Home", updated.getCategory());
        assertEquals(new BigDecimal("500.00"), updated.getPrice());
        assertTrue(updated.getFeatured());
    }
}
