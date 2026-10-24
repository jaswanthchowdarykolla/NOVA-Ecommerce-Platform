package com.nova.service;

import com.nova.product.Product;
import com.nova.product.ProductRepository;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {
    private final ProductRepository repo;

    public ProductService(ProductRepository repo) { this.repo = repo; }

    public List<Product> all() { return repo.findAll(); }

    public Product get(long id) {
        return repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    @Transactional
    public Product create(ProductInput input) {
        ProductInput withoutClientId = new ProductInput(null, input.name(), input.slug(), input.category(), input.brand(),
                input.price(), input.rating(), input.reviewCount(), input.description(), input.imageUrl(),
                input.color(), input.featured(), input.tags(), input.availableSizes());
        return save(withoutClientId);
    }

    @Transactional
    public Product save(ProductInput input) {
        validate(input);
        long id = input.id() != null ? input.id() : repo.findMaxId() + 1;
        String slug = input.slug() == null || input.slug().isBlank() ? slug(input.name()) + "-" + id : slug(input.slug());
        Product p = new Product(id, input.name().trim(), slug, input.category().trim(),
                input.brand().trim(), input.price(), input.rating() == null ? 4.5 : input.rating(),
                input.reviewCount() == null ? 0 : input.reviewCount(), input.description() == null ? "" : input.description().trim(),
                input.imageUrl() == null ? "" : input.imageUrl().trim(), input.color() == null ? "" : input.color().trim(),
                input.featured() != null && input.featured(), input.tags() == null ? input.category().toLowerCase() : input.tags().trim(),
                normalizeSizes(input.availableSizes()));
        return repo.save(p);
    }

    @Transactional
    public Product update(long id, ProductInput in) {
        Product old = get(id);
        ProductInput merged = new ProductInput(id,
                in.name() == null ? old.getName() : in.name(),
                in.slug() == null ? old.getSlug() : in.slug(),
                in.category() == null ? old.getCategory() : in.category(),
                in.brand() == null ? old.getBrand() : in.brand(),
                in.price() == null ? old.getPrice() : in.price(),
                in.rating() == null ? old.getRating() : in.rating(),
                in.reviewCount() == null ? old.getReviewCount() : in.reviewCount(),
                in.description() == null ? old.getDescription() : in.description(),
                in.imageUrl() == null ? old.getImageUrl() : in.imageUrl(),
                in.color() == null ? old.getColor() : in.color(),
                in.featured() == null ? old.getFeatured() : in.featured(),
                in.tags() == null ? old.getTags() : in.tags(),
                in.availableSizes() == null ? old.getAvailableSizes() : in.availableSizes());
        return save(merged);
    }

    @Transactional
    public void delete(long id) {
        if (!repo.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        repo.deleteById(id);
    }

    private void validate(ProductInput input) {
        if (input.name() == null || input.name().isBlank() || input.name().length() > 255) bad("Product name is required (maximum 255 characters).");
        if (input.category() == null || input.category().isBlank() || input.category().length() > 120) bad("Category is required (maximum 120 characters).");
        if (input.brand() == null || input.brand().isBlank() || input.brand().length() > 120) bad("Brand is required (maximum 120 characters).");
        if (input.price() == null || input.price().compareTo(BigDecimal.ZERO) < 0) bad("Price must be zero or greater.");
        if (input.rating() != null && (input.rating() < 0 || input.rating() > 5)) bad("Rating must be between 0 and 5.");
        if (input.reviewCount() != null && input.reviewCount() < 0) bad("Review count cannot be negative.");
        if (input.imageUrl() != null && !input.imageUrl().isBlank() && !input.imageUrl().startsWith("https://")) bad("Product image URL must use HTTPS.");
        if (input.availableSizes() != null && input.availableSizes().length() > 200) bad("Available sizes cannot exceed 200 characters.");
    }
    private void bad(String message) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message); }
    private String slug(String value) { return value.toLowerCase().trim().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", ""); }
    private String normalizeSizes(String raw) {
        if (raw == null || raw.isBlank()) return null;
        return String.join(",", com.nova.product.ProductSizes.parse(raw));
    }

    public record ProductInput(Long id,
            @NotBlank @Size(max = 255) String name,
            @Size(max = 255) String slug,
            @NotBlank @Size(max = 120) String category,
            @NotBlank @Size(max = 120) String brand,
            @NotNull @DecimalMin("0.00") BigDecimal price,
            Double rating, Integer reviewCount, @Size(max = 1200) String description,
            @Size(max = 800) String imageUrl, @Size(max = 80) String color,
            Boolean featured, @Size(max = 800) String tags, @Size(max = 200) String availableSizes) { }
}
