package com.nova.addresses;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class AddressDtos {
    private AddressDtos() {}

    public record AddressRequest(
            @NotBlank @Size(max = 100) String recipientName,
            @NotBlank @Pattern(regexp = "^\\+?[0-9 ()-]{8,18}$", message = "Enter a valid phone number") String phone,
            @NotBlank @Size(max = 255) String addressLine1,
            @Size(max = 255) String addressLine2,
            @Size(max = 120) String locality,
            @NotBlank @Size(max = 100) String city,
            @NotBlank @Size(max = 100) String state,
            @NotBlank @Size(max = 20) String postalCode,
            @Size(max = 100) String country,
            Boolean isDefault
    ) {}

    public record AddressResponse(
            long id,
            String recipientName,
            String phone,
            String addressLine1,
            String addressLine2,
            String locality,
            String city,
            String state,
            String postalCode,
            String country,
            boolean isDefault,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static AddressResponse from(AddressRecord a) {
            return new AddressResponse(
                    a.getId() != null ? a.getId() : 0L,
                    a.getRecipientName(),
                    a.getPhone(),
                    a.getAddressLine1(),
                    a.getAddressLine2(),
                    a.getLocality(),
                    a.getCity(),
                    a.getState(),
                    a.getPostalCode(),
                    a.getCountry(),
                    a.getIsDefault() != null && a.getIsDefault(),
                    a.getCreatedAt(),
                    a.getUpdatedAt()
            );
        }
    }
}
