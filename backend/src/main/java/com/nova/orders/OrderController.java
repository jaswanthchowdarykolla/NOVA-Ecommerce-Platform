package com.nova.orders;

import com.nova.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDtos.Summary create(Authentication auth, @Valid @RequestBody OrderDtos.Create req) {
        return service.create(auth.getName(), req);
    }

    @GetMapping("/mine")
    public List<OrderDtos.Summary> mine(Authentication auth) {
        return service.mine(auth.getName());
    }

    @GetMapping("/{id}")
    public OrderDtos.Summary one(Authentication auth, @PathVariable long id) {
        return OrderDtos.Summary.from(service.owned(id, auth.getName()));
    }
}
