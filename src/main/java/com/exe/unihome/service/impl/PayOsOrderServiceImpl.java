package com.exe.unihome.service.impl;

import com.exe.unihome.dto.ConfirmWebhookRequest;
import com.exe.unihome.dto.CreatePaymentLinkRequestBody;
import com.exe.unihome.service.PayOsOrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLink;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import vn.payos.model.webhooks.ConfirmWebhookResponse;

@Service
@RequiredArgsConstructor
public class PayOsOrderServiceImpl implements PayOsOrderService {

  private final PayOS payOS;
  private final ObjectMapper objectMapper;

  @Value("${payment.minutes}")
  private long EXPIRED_MINUTES;

  @Override
  public ObjectNode createPaymentLink(CreatePaymentLinkRequestBody request) {
    ObjectNode response = objectMapper.createObjectNode();

    try {
      long orderCode = System.currentTimeMillis() / 1000;

      PaymentLinkItem item = PaymentLinkItem.builder()
        .name(request.getProductName())
        .quantity(1)
        .price((long) request.getPrice())
        .build();

//      long expiredAt = Instant.now()
//        .plus(EXPIRED_MINUTES, ChronoUnit.MINUTES)
//        .getEpochSecond();
      long expiredAt = request.getExpiredAt();

      CreatePaymentLinkRequest paymentRequest =
        CreatePaymentLinkRequest.builder()
          .orderCode(orderCode)
          .description(request.getDescription())
          .amount((long) request.getPrice())
          .returnUrl(request.getReturnUrl())
          .cancelUrl(request.getCancelUrl())
          .expiredAt(expiredAt)
          .build();

      CreatePaymentLinkResponse data =
        payOS.paymentRequests().create(paymentRequest);

      response.put("error", 0);
      response.put("message", "success");
      response.set("data", objectMapper.valueToTree(data));

      return response;

    } catch (Exception e) {
      e.printStackTrace();
      response.put("error", -1);
      response.put("message", e.getMessage());
      response.set("data", null);
      return response;
    }
  }

  @Override
  public ObjectNode getOrderById(long orderId) {
    ObjectNode response = objectMapper.createObjectNode();

    try {
      PaymentLink order = payOS.paymentRequests().get(orderId);

      response.put("error", 0);
      response.put("message", "ok");
      response.set("data", objectMapper.valueToTree(order));

      return response;

    } catch (Exception e) {
      e.printStackTrace();
      response.put("error", -1);
      response.put("message", e.getMessage());
      response.set("data", null);
      return response;
    }
  }

  @Override
  public ObjectNode cancelOrder(long orderId) {
    ObjectNode response = objectMapper.createObjectNode();

    try {
      PaymentLink order =
        payOS.paymentRequests().cancel(orderId, "change my mind");

      response.put("error", 0);
      response.put("message", "ok");
      response.set("data", objectMapper.valueToTree(order));

      return response;

    } catch (Exception e) {
      e.printStackTrace();
      response.put("error", -1);
      response.put("message", e.getMessage());
      response.set("data", null);
      return response;
    }
  }

  @Override
  public ObjectNode confirmWebhook(ConfirmWebhookRequest request) {
    ObjectNode response = objectMapper.createObjectNode();

    try {
      ConfirmWebhookResponse result =
        payOS.webhooks().confirm(request.getWebhookUrl());

      response.put("error", 0);
      response.put("message", "ok");
      response.set("data", objectMapper.valueToTree(result));

      return response;

    } catch (Exception e) {
      e.printStackTrace();
      response.put("error", -1);
      response.put("message", e.getMessage());
      response.set("data", null);
      return response;
    }
  }
}
