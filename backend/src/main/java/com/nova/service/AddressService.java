package com.nova.service;

import com.nova.addresses.AddressDtos;
import com.nova.addresses.AddressRecord;
import com.nova.addresses.AddressRepository;
import com.nova.auth.User;
import com.nova.auth.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AddressService {
    private final AddressRepository addresses;
    private final UserRepository users;

    public AddressService(AddressRepository addresses, UserRepository users) {
        this.addresses = addresses;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<AddressDtos.AddressResponse> listMine(String email) {
        return addresses.findByUser_EmailOrderByIsDefaultDescCreatedAtDesc(email)
                .stream()
                .map(AddressDtos.AddressResponse::from)
                .toList();
    }

    @Transactional
    public AddressDtos.AddressResponse create(String email, AddressDtos.AddressRequest req) {
        User user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User account not found"));

        long existingCount = addresses.countByUser_Email(email);
        boolean shouldBeDefault = existingCount == 0 || (req.isDefault() != null && req.isDefault());

        if (shouldBeDefault) {
            addresses.clearDefaultAddresses(email);
        }

        AddressRecord saved = addresses.save(new AddressRecord(
                user,
                req.recipientName().trim(),
                req.phone().trim(),
                req.addressLine1().trim(),
                req.addressLine2() != null ? req.addressLine2().trim() : null,
                req.locality() != null ? req.locality().trim() : null,
                req.city().trim(),
                req.state().trim(),
                req.postalCode().trim(),
                req.country() != null && !req.country().isBlank() ? req.country().trim() : "India",
                shouldBeDefault
        ));

        return AddressDtos.AddressResponse.from(saved);
    }

    @Transactional
    public AddressDtos.AddressResponse update(String email, Long id, AddressDtos.AddressRequest req) {
        AddressRecord address = addresses.findByIdAndUser_Email(id, email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found or does not belong to you"));

        boolean shouldBeDefault = req.isDefault() != null ? req.isDefault() : address.getIsDefault();
        if (shouldBeDefault && !address.getIsDefault()) {
            addresses.clearDefaultAddresses(email);
        }

        address.update(
                req.recipientName().trim(),
                req.phone().trim(),
                req.addressLine1().trim(),
                req.addressLine2() != null ? req.addressLine2().trim() : null,
                req.locality() != null ? req.locality().trim() : null,
                req.city().trim(),
                req.state().trim(),
                req.postalCode().trim(),
                req.country() != null && !req.country().isBlank() ? req.country().trim() : "India",
                shouldBeDefault
        );

        return AddressDtos.AddressResponse.from(address);
    }

    @Transactional
    public void delete(String email, Long id) {
        AddressRecord address = addresses.findByIdAndUser_Email(id, email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found or does not belong to you"));

        boolean wasDefault = address.getIsDefault();
        addresses.delete(address);

        if (wasDefault) {
            List<AddressRecord> remaining = addresses.findByUser_EmailOrderByIsDefaultDescCreatedAtDesc(email);
            if (!remaining.isEmpty()) {
                remaining.get(0).setDefault(true);
            }
        }
    }

    @Transactional
    public AddressDtos.AddressResponse setDefault(String email, Long id) {
        AddressRecord address = addresses.findByIdAndUser_Email(id, email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found or does not belong to you"));

        addresses.clearDefaultAddresses(email);
        address.setDefault(true);
        return AddressDtos.AddressResponse.from(address);
    }
}
