package com.nova.addresses;

import com.nova.auth.User;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "addresses", indexes = {@Index(name = "idx_addresses_user_id", columnList = "user_id")})
public class AddressRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "recipient_name", nullable = false, length = 100)
    private String recipientName;

    @Column(nullable = false, length = 30)
    private String phone;

    @Column(name = "address_line1", nullable = false, length = 255)
    private String addressLine1;

    @Column(name = "address_line2", length = 255)
    private String addressLine2;

    @Column(length = 120)
    private String locality;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String state;

    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;

    @Column(nullable = false, length = 100)
    private String country = "India";

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected AddressRecord() {}

    public AddressRecord(User user, String recipientName, String phone, String addressLine1,
                         String addressLine2, String locality, String city, String state,
                         String postalCode, String country, boolean isDefault) {
        this.user = user;
        this.recipientName = recipientName;
        this.phone = phone;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.locality = locality;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country != null && !country.isBlank() ? country : "India";
        this.isDefault = isDefault;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public String getRecipientName() { return recipientName; }
    public String getPhone() { return phone; }
    public String getAddressLine1() { return addressLine1; }
    public String getAddressLine2() { return addressLine2; }
    public String getLocality() { return locality; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getPostalCode() { return postalCode; }
    public String getCountry() { return country; }
    public Boolean getIsDefault() { return isDefault; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void update(String recipientName, String phone, String addressLine1,
                       String addressLine2, String locality, String city, String state,
                       String postalCode, String country, Boolean isDefault) {
        this.recipientName = recipientName;
        this.phone = phone;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.locality = locality;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        if (country != null && !country.isBlank()) this.country = country;
        if (isDefault != null) this.isDefault = isDefault;
        this.updatedAt = Instant.now();
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
        this.updatedAt = Instant.now();
    }
}
