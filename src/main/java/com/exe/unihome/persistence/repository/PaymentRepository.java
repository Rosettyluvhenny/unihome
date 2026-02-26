package com.exe.unihome.persistence.repository;

import com.exe.unihome.persistence.entity.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

  /**
   * Find payment by name
   */
  Optional<Payment> findByName(String name);

  /**
   * Find active payments
   */
  Optional<Payment> findByIdAndIsActiveTrue(String id);
}

