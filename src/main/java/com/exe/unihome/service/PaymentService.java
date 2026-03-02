package com.exe.unihome.service;

import com.exe.unihome.dto.payment.request.CreatePaymentRequest;
import com.exe.unihome.dto.payment.request.UpdatePaymentRequest;
import com.exe.unihome.dto.payment.response.PaymentResponse;

import java.util.List;

public interface PaymentService {

  /**
   * Create a new payment method
   */
  PaymentResponse createPayment(CreatePaymentRequest request);

  /**
   * Get payment by ID
   */
  PaymentResponse getPaymentById(String id);

  /**
   * Get all payments
   */
  List<PaymentResponse> getAllPayments();

  /**
   * Get all active payments
   */
  List<PaymentResponse> getActivePayments();

  /**
   * Update payment
   */
  PaymentResponse updatePayment(String id, UpdatePaymentRequest request);

  /**
   * Delete (deactivate) payment
   */
  void deletePayment(String id);

  /**
   * Check if payment exists
   */
  boolean existsById(String id);
}

