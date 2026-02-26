package com.exe.unihome.Payment;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.react05.fcinema_spring.model.request.ConfirmWebhookRequest;
import com.react05.fcinema_spring.model.request.CreatePaymentLinkRequest;
import com.react05.fcinema_spring.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {
  private final OrderService orderService;

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
