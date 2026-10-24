package com.nova.product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
public interface ProductRepository extends JpaRepository<Product,Long> {
 Optional<Product> findBySlug(String slug);
 List<Product> findByCategoryIn(List<String> categories);
 @Query("select p from Product p where (cast(:q as string) is null or lower(p.name) like lower(cast(:q as string)) or lower(p.description) like lower(cast(:q as string)) or lower(p.brand) like lower(cast(:q as string)) or lower(p.category) like lower(cast(:q as string)) or lower(coalesce(p.tags, '')) like lower(cast(:q as string))) and (cast(:category as string) is null or lower(p.category) = lower(cast(:category as string))) and (:maxPrice is null or p.price <= :maxPrice)")
 Page<Product> search(@Param("q") String q, @Param("category") String category, @Param("maxPrice") BigDecimal maxPrice, Pageable pageable);
 List<Product> findTop12ByFeaturedTrueOrderByIdAsc();
 @Query("select coalesce(max(p.id), 0) from Product p")
 long findMaxId();
}
