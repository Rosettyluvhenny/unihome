package com.exe.unihome.service;

import com.exe.unihome.dto.ConfirmWebhookRequest;
import com.exe.unihome.dto.CreatePaymentLinkRequest;
import com.fasterxml.jackson.databind.node.ObjectNode;

public interface PayOsOrderService {
  ObjectNode createPaymentLink(CreatePaymentLinkRequest request);

  ObjectNode getOrderById(long orderId);

  ObjectNode cancelOrder(int orderId);

  ObjectNode confirmWebhook(ConfirmWebhookRequest requestBody);
}
