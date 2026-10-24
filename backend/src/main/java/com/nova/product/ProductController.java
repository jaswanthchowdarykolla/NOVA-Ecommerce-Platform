package com.nova.product;
import com.nova.service.ProductService;

import org.springframework.data.domain.Page; import org.springframework.data.domain.PageRequest; import org.springframework.data.domain.Sort; import org.springframework.http.HttpStatus; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.web.bind.annotation.*; import jakarta.validation.Valid; import java.math.BigDecimal; import java.util.Map;
@RestController @RequestMapping("/api")
public class ProductController {
 private final ProductRepository repo; private final ProductService service; public ProductController(ProductRepository repo,ProductService service){this.repo=repo;this.service=service;}
 @GetMapping("/health") public Map<String,String> health(){return Map.of("status","ok","service","nova-api");}
 @GetMapping("/products") public Page<Product> search(@RequestParam(required=false) String q,@RequestParam(required=false) String category,@RequestParam(required=false) BigDecimal maxPrice,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="24") int size,@RequestParam(defaultValue="id") String sort,@RequestParam(defaultValue="asc") String direction){String field=switch(sort){case "price"->"price";case "rating"->"rating";case "name"->"name";default->"id";};Sort s="desc".equalsIgnoreCase(direction)?Sort.by(field).descending():Sort.by(field).ascending();String qPattern = blank(q) != null ? "%" + blank(q).toLowerCase() + "%" : null; return repo.search(qPattern,blank(category),maxPrice,PageRequest.of(Math.max(page,0),Math.min(Math.max(size,1),1000),s));}
 @GetMapping("/products/featured") public Object featured(){return repo.findTop12ByFeaturedTrueOrderByIdAsc();}
 @GetMapping("/products/{id}") public Product one(@PathVariable long id){return service.get(id);}
 @PostMapping("/products") @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasRole('ADMIN')") public Product create(@Valid @RequestBody ProductService.ProductInput in){return service.create(in);}
 @PutMapping("/products/{id}") @PreAuthorize("hasRole('ADMIN')") public Product update(@PathVariable long id,@Valid @RequestBody ProductService.ProductInput in){return service.update(id,in);}
 @DeleteMapping("/products/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasRole('ADMIN')") public void delete(@PathVariable long id){service.delete(id);}
 private String blank(String s){return s==null||s.isBlank()?null:s.trim();}
}
