package com.exe.unihome.controller;

import com.exe.unihome.common.model.ApiResponse;
import com.exe.unihome.dto.payment.request.CreatePaymentRequest;
import com.exe.unihome.dto.payment.request.UpdatePaymentRequest;
import com.exe.unihome.dto.payment.response.PaymentResponse;
import com.exe.unihome.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment method management APIs")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

  private final PaymentService paymentService;

  @PostMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
  @Operation(summary = "Create a new payment method",
    description = "Create a new payment method (e.g., PayOS, etc.)")
  public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
    @Valid @RequestBody CreatePaymentRequest request) {
    PaymentResponse response = paymentService.createPayment(request);
    return ResponseEntity.status(HttpStatus.CREATED)
      .body(ApiResponse.<PaymentResponse>builder()
        .code(0)
        .message("Payment method created successfully")
        .data(response)
        .build());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get payment method by ID")
  public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(
    @PathVariable String id) {
    PaymentResponse response = paymentService.getPaymentById(id);
    return ResponseEntity.ok(ApiResponse.<PaymentResponse>builder()
      .code(0)
      .message("Success")
      .data(response)
      .build());
  }

  @GetMapping
  @Operation(summary = "Get all payment methods")
  public ResponseEntity<ApiResponse<List<PaymentResponse>>> getAllPayments() {
    List<PaymentResponse> response = paymentService.getAllPayments();
    return ResponseEntity.ok(ApiResponse.<List<PaymentResponse>>builder()
      .code(0)
      .message("Success")
      .data(response)
      .build());
  }

  @GetMapping("/active/list")
  @Operation(summary = "Get all active payment methods")
  public ResponseEntity<ApiResponse<List<PaymentResponse>>> getActivePayments() {
    List<PaymentResponse> response = paymentService.getActivePayments();
    return ResponseEntity.ok(ApiResponse.<List<PaymentResponse>>builder()
      .code(0)
      .message("Success")
      .data(response)
      .build());
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
  @Operation(summary = "Update payment method",
    description = "Update payment method name and/or active status")
  public ResponseEntity<ApiResponse<PaymentResponse>> updatePayment(
    @PathVariable String id,
    @Valid @RequestBody UpdatePaymentRequest request) {
    PaymentResponse response = paymentService.updatePayment(id, request);
    return ResponseEntity.ok(ApiResponse.<PaymentResponse>builder()
      .code(0)
      .message("Payment method updated successfully")
      .data(response)
      .build());
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Delete (deactivate) payment method",
    description = "Deactivate a payment method. This is a soft delete.")
  public ResponseEntity<ApiResponse<Void>> deletePayment(@PathVariable String id) {
    paymentService.deletePayment(id);
    return ResponseEntity.ok(ApiResponse.<Void>builder()
      .code(0)
      .message("Payment method deleted successfully")
      .build());
  }
}

