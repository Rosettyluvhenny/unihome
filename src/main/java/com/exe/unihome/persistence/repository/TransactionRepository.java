package com.exe.unihome.persistence.repository;

import com.exe.unihome.persistence.entity.payment.Transaction;
import com.exe.unihome.persistence.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

  /**
   * Find transaction by order ID
   */
  Optional<Transaction> findByOrder_OrderId(UUID orderId);

  /**
   * Find all transactions with specific status
   */
  List<Transaction> findByStatus(TransactionStatus status);

  /**
   * Find pending transactions that have expired
   */
  List<Transaction> findByStatusAndExpiredAtBefore(TransactionStatus status, LocalDateTime currentTime);

  /**
   * Find all pending transactions
   */
  List<Transaction> findByStatusOrderByCreatedAtAsc(TransactionStatus status);

  /**
   * Count pending transactions for an order
   */
  long countByOrder_OrderIdAndStatus(UUID orderId, TransactionStatus status);

  Optional<Transaction> findByPayOsCode(String payOsCode);
}

