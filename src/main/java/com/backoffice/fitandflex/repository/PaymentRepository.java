package com.backoffice.fitandflex.repository;

import com.backoffice.fitandflex.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
