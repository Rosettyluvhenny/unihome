package com.exe.unihome.config;

import com.exe.unihome.persistence.entity.payment.Payment;
import com.exe.unihome.persistence.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Payment Seeder
 * Seeds initial payment methods (ONLINE and CASH) into the database
 * Runs automatically on application startup
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentSeeder implements CommandLineRunner {

  private final PaymentRepository paymentRepository;

  @Override
  public void run(String... args) throws Exception {
    seedPaymentMethods();
  }

  /**
   * Seed ONLINE and CASH payment methods
   * Creates payment records if they don't already exist
   */
  public void seedPaymentMethods() {
    try {
      log.info("Starting payment methods seeding...");

      // Seed ONLINE payment method
      if (!paymentRepository.existsById("ONLINE")) {
        Payment onlinePayment = Payment.builder()
          .id("ONLINE")
          .name("Online Payment")
          .isActive(true)
          .img("https://payos.ai/og-payos-mastercard.png")
          .build();
        paymentRepository.save(onlinePayment);
        log.info("✓ ONLINE payment method created");
      } else {
        log.info("✓ ONLINE payment method already exists");
      }

      // Seed CASH payment method
      if (!paymentRepository.existsById("CASH")) {
        Payment cashPayment = Payment.builder()
          .id("CASH")
          .name("Cash on Delivery")
          .isActive(true)
          .img("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTB5GQxnEEcqpUOYsqnDdi1B482KikTd0bQJw&s")
          .build();
        paymentRepository.save(cashPayment);
        log.info("✓ CASH payment method created");
      } else {
        log.info("✓ CASH payment method already exists");
      }

      log.info("Payment methods seeding completed successfully");

    } catch (Exception e) {
      log.error("Error seeding payment methods: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to seed payment methods", e);
    }
  }
}

