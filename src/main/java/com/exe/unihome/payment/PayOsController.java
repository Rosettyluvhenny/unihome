package com.exe.unihome.payment;

import com.exe.unihome.common.model.ApiResponse;
import com.exe.unihome.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.payos.PayOS;
import vn.payos.model.webhooks.WebhookData;

@RestController
@RequestMapping("/payos")
@RequiredArgsConstructor
@Slf4j
public class PayOsController {
  private final PayOS payOS;
  private final TransactionService transactionService;

  @PostMapping("/payos_transfer_handler")
  public ApiResponse<WebhookData> payosTransferHandler(@RequestBody Object body) {

    try {
      log.info("Received PayOS webhook raw body: {}", body);

      // ✅ Verify webhook (SDK v2)
      WebhookData data = payOS.webhooks().verify(body);

      log.info("Webhook verified successfully: {}", data);

      // ✅ Business logic
      if ("success".equalsIgnoreCase(data.getDesc())) {
        handleSuccessfulPayment(data);
      } else {
        handleCancelledPayment(data);
      }

      return ApiResponse.<WebhookData>builder()
        .code(0)
        .message("Webhook delivered")
        .data(data)
        .build();

    } catch (Exception e) {
      log.error("Error processing PayOS webhook", e);

      return ApiResponse.<WebhookData>builder()
        .code(-1)
        .message(e.getMessage())
        .data(null)
        .build();
    }
  }

  private void handleSuccessfulPayment(WebhookData data) {
    try {
      Long orderCode = data.getOrderCode();
      log.info("Processing successful payment for order: {}", orderCode);

      transactionService.confirmTransaction(String.valueOf(orderCode));

      log.info("Payment successful - Order: {}, Amount: {}, Description: {}",
        orderCode, data.getAmount(), data.getDescription());

    } catch (Exception e) {
      log.error("Error handling successful payment", e);
    }
  }

  private void handleCancelledPayment(WebhookData data) {
    try {
      Long orderCode = data.getOrderCode();
      log.info("Processing cancelled payment for order: {}", orderCode);

      transactionService.cancelTransaction(
        String.valueOf(orderCode),
        "Khách hàng hủy thanh toán"
      );

    } catch (Exception e) {
      log.error("Error handling cancelled payment", e);
    }
  }
}
