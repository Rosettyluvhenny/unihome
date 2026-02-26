package com.exe.unihome.Payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.react05.fcinema_spring.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.payos.PayOS;
import vn.payos.type.Webhook;
import vn.payos.type.WebhookData;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
  private final PayOS payOS;
  private final BookingService bookingService;

  @PostMapping(path = "/payos_transfer_handler")
  public ObjectNode payosTransferHandler(@RequestBody ObjectNode body)
    throws JsonProcessingException, IllegalArgumentException {

    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode response = objectMapper.createObjectNode();
    Webhook webhookBody = objectMapper.treeToValue(body, Webhook.class);
    log.info("Received PayOS webhook controller: {}", webhookBody);
    try {
      log.info("Received PayOS webhook: {}", body);

      // Verify webhook data with PayOS
      WebhookData data = payOS.verifyPaymentWebhookData(webhookBody);
      log.info("Payment webhook verified successfully: {}", data);

      // Process payment based on status
      if (data.getDesc().equals("success")) {
        handleSuccessfulPayment(data);
        log.info("successful payment for order: {}", data.getOrderCode());
      } else {
        handleCancelledPayment(data);
        log.info("cancelled payment for order: {}", data.getOrderCode());
      }
      // Init Response
      response.put("error", 0);
      response.put("message", "Webhook delivered");
      response.set("data", objectMapper.valueToTree(data));

      return response;
    } catch (Exception e) {
      log.error("Error processing PayOS webhook", e);
      response.put("error", -1);
      response.put("message", e.getMessage());
      response.set("data", null);
      return response;
    }
  }

  private void handleSuccessfulPayment(WebhookData data) {
    try {
      Long orderCode = data.getOrderCode();
      log.info("Processing successful payment for order: {}", orderCode);
      bookingService.confirmWebhookPayment(orderCode);
      // Here you would typically find the booking by order code
      // For now, we'll log the payment success
      // You can add logic to update booking status based on your business needs

      log.info("Payment successful - Order: {}, Amount: {}, Description: {}",
        orderCode, data.getAmount(), data.getDescription());

      // TODO: Update booking payment status to PAID
      // Example: bookingRepository.updatePaymentStatusByOrderCode(orderCode, Booking.PaymentStatus.PAID);

    } catch (Exception e) {
      log.error("Error handling successful payment", e);
    }
  }

  private void handleCancelledPayment(WebhookData data) {
    try {
      Long orderCode = data.getOrderCode();
      log.info("Processing cancelled payment for order: {}", orderCode);
      bookingService.cancelWebhookPayment(orderCode);
      // TODO: Update booking payment status to CANCELLED
      // Example: bookingRepository.updatePaymentStatusByOrderCode(orderCode, Booking.PaymentStatus.CANCELLED);

    } catch (Exception e) {
      log.error("Error handling cancelled payment", e);
    }
  }
}
