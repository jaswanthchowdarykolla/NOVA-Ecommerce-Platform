package com.nova.addresses;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<AddressRecord, Long> {
    List<AddressRecord> findByUser_EmailOrderByIsDefaultDescCreatedAtDesc(String email);
    Optional<AddressRecord> findByIdAndUser_Email(Long id, String email);
    long countByUser_Email(String email);

    @Modifying
    @Query("update AddressRecord a set a.isDefault = false where a.user.email = :email")
    void clearDefaultAddresses(@Param("email") String email);
}
