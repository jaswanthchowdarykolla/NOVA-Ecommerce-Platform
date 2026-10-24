package com.nova.payments;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentRecord, Long> {
    boolean existsByOrder_Id(Long orderId);
    Optional<PaymentRecord> findByOrder_Id(Long orderId);
    Optional<PaymentRecord> findByRazorpayOrderId(String razorpayOrderId);
    List<PaymentRecord> findByOrder_User_EmailOrderByUpdatedAtDesc(String email);
}
