package com.exe.unihome.payment;

import com.exe.unihome.dto.ConfirmWebhookRequest;
import com.exe.unihome.dto.CreatePaymentLinkRequest;
import com.exe.unihome.service.PayOsOrderService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/testOrder")
@RequiredArgsConstructor
public class PayOsOrderController {
  private final PayOsOrderService orderService;

  @PostMapping(path = "/create")
  public ObjectNode createPaymentLink(@RequestBody CreatePaymentLinkRequest request) {
    return orderService.createPaymentLink(request);
  }

  @GetMapping(path = "/{orderId}")
  public ObjectNode getOrderById(@PathVariable("orderId") long orderId) {
    return orderService.getOrderById(orderId);
  }

  @PutMapping(path = "/{orderId}")
  public ObjectNode cancelOrder(@PathVariable("orderId") int orderId) {
    return orderService.cancelOrder(orderId);
  }

  @PostMapping(path = "/confirm-webhook")
  public ObjectNode confirmWebhook(@RequestBody ConfirmWebhookRequest request) {
    return orderService.confirmWebhook(request);
  }
}
