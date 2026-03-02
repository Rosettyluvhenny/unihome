package com.exe.unihome.service;

import com.exe.unihome.dto.ConfirmWebhookRequest;
import com.exe.unihome.dto.CreatePaymentLinkRequestBody;
import com.fasterxml.jackson.databind.node.ObjectNode;

public interface PayOsOrderService {
  ObjectNode createPaymentLink(CreatePaymentLinkRequestBody request);

  ObjectNode getOrderById(long orderId);

  ObjectNode cancelOrder(long orderId);

  ObjectNode confirmWebhook(ConfirmWebhookRequest requestBody);
}
