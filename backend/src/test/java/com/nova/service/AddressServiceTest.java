package com.nova.service;

import com.nova.addresses.AddressDtos;
import com.nova.addresses.AddressRecord;
import com.nova.addresses.AddressRepository;
import com.nova.auth.Role;
import com.nova.auth.User;
import com.nova.auth.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AddressService addressService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("Jane Doe", "jane@example.com", "secret123", Role.CUSTOMER);
    }

    @Test
    void createFirstAddressBecomesDefaultAutomatically() {
        when(userRepository.findByEmailIgnoreCase("jane@example.com")).thenReturn(Optional.of(testUser));
        when(addressRepository.countByUser_Email("jane@example.com")).thenReturn(0L);
        when(addressRepository.save(any(AddressRecord.class))).thenAnswer(inv -> {
            AddressRecord a = inv.getArgument(0);
            a.setId(1L);
            return a;
        });

        AddressDtos.AddressRequest req = new AddressDtos.AddressRequest(
                "Jane Doe", "+91 9876543210", "42 Heritage Road", "Apt 4B",
                "Indiranagar", "Bengaluru", "Karnataka", "560038", "India", false
        );

        AddressDtos.AddressResponse res = addressService.create("jane@example.com", req);

        assertTrue(res.isDefault(), "First address should automatically be marked default");
        assertEquals("Bengaluru", res.city());
        assertEquals("560038", res.postalCode());
        assertEquals(1L, res.id());
        verify(addressRepository).clearDefaultAddresses("jane@example.com");
    }

    @Test
    void createSubsequentAddressWithoutDefaultLeavesPreviousDefault() {
        when(userRepository.findByEmailIgnoreCase("jane@example.com")).thenReturn(Optional.of(testUser));
        when(addressRepository.countByUser_Email("jane@example.com")).thenReturn(1L);
        when(addressRepository.save(any(AddressRecord.class))).thenAnswer(inv -> {
            AddressRecord a = inv.getArgument(0);
            a.setId(2L);
            return a;
        });

        AddressDtos.AddressRequest req = new AddressDtos.AddressRequest(
                "Jane Doe", "+91 9876543210", "12 Palm Street", null,
                null, "Bengaluru", "Karnataka", "560001", "India", false
        );

        AddressDtos.AddressResponse res = addressService.create("jane@example.com", req);

        assertFalse(res.isDefault(), "Second address without isDefault=true should not be default");
        assertEquals(2L, res.id());
        verify(addressRepository, never()).clearDefaultAddresses("jane@example.com");
    }

    @Test
    void setDefaultClearsPreviousAndSetsNew() {
        AddressRecord addr = new AddressRecord(
                testUser, "Jane Doe", "+91 9876543210", "42 Heritage Road",
                null, null, "Bengaluru", "Karnataka", "560038", "India", false
        );
        addr.setId(5L);
        when(addressRepository.findByIdAndUser_Email(5L, "jane@example.com")).thenReturn(Optional.of(addr));

        AddressDtos.AddressResponse res = addressService.setDefault("jane@example.com", 5L);

        assertTrue(res.isDefault());
        assertEquals(5L, res.id());
        verify(addressRepository).clearDefaultAddresses("jane@example.com");
    }

    @Test
    void deleteNonExistentAddressThrowsNotFound() {
        when(addressRepository.findByIdAndUser_Email(99L, "jane@example.com")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> addressService.delete("jane@example.com", 99L));
        verify(addressRepository, never()).delete(any());
    }
}
