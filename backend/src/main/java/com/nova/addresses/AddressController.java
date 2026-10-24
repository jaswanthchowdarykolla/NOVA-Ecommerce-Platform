package com.nova.addresses;

import com.nova.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {
    private final AddressService service;

    public AddressController(AddressService service) {
        this.service = service;
    }

    @GetMapping
    public List<AddressDtos.AddressResponse> list(Authentication auth) {
        return service.listMine(auth.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AddressDtos.AddressResponse create(Authentication auth, @Valid @RequestBody AddressDtos.AddressRequest req) {
        return service.create(auth.getName(), req);
    }

    @PutMapping("/{id}")
    public AddressDtos.AddressResponse update(Authentication auth, @PathVariable Long id, @Valid @RequestBody AddressDtos.AddressRequest req) {
        return service.update(auth.getName(), id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication auth, @PathVariable Long id) {
        service.delete(auth.getName(), id);
    }

    @PutMapping("/{id}/default")
    public AddressDtos.AddressResponse setDefault(Authentication auth, @PathVariable Long id) {
        return service.setDefault(auth.getName(), id);
    }
}
