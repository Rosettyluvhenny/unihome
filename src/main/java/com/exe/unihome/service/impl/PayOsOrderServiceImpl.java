package com.exe.unihome.service.impl;

import com.exe.unihome.dto.ConfirmWebhookRequest;
import com.exe.unihome.dto.CreatePaymentLinkRequest;
import com.exe.unihome.service.PayOsOrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;
import vn.payos.type.PaymentLinkData;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class PayOsOrderServiceImpl implements PayOsOrderService {
  private final PayOS payOS;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public ObjectNode createPaymentLink(CreatePaymentLinkRequest request) {
    ObjectNode response = objectMapper.createObjectNode();
    try {
      // Add logging to debug
      System.out.println("Received request: " + request);
      System.out.println("Product name: " + request.getProductName());
      System.out.println("Price: " + request.getPrice());

      final String productName = request.getProductName();
      final String description = request.getDescription();
      final String returnUrl = request.getReturnUrl();
      final String cancelUrl = request.getCancelUrl();
      final int price = request.getPrice();

      // Validate required fields
      if (productName == null || productName.trim().isEmpty()) {
        response.put("error", -1);
        response.put("message", "Product name is required");
        response.set("data", null);
        return response;
      }

      if (price <= 0) {
        response.put("error", -1);
        response.put("message", "Price must be greater than 0");
        response.set("data", null);
        return response;
      }

      // Gen order code
      String currentTimeString = String.valueOf(new Date().getTime());
      long orderCode = Long.parseLong(currentTimeString.substring(currentTimeString.length() - 6));

      System.out.println("Generated order code: " + orderCode);

      ItemData item = ItemData.builder().name(productName).price(price).quantity(1).build();

      PaymentData paymentData = PaymentData.builder()
        .orderCode(orderCode)
        .description(description)
        .amount(price)
        .item(item)
        .returnUrl(returnUrl)
        .cancelUrl(cancelUrl)
        .build();

      System.out.println("Creating payment link with PayOS...");
      CheckoutResponseData data = payOS.createPaymentLink(paymentData);
      System.out.println("Payment link created successfully: " + data.getCheckoutUrl());

      response.put("error", 0);
      response.put("message", "success");
      response.set("data", objectMapper.valueToTree(data));
      return response;

    } catch (Exception e) {
      System.err.println("Error creating payment link: " + e.getMessage());
      e.printStackTrace();
      response.put("error", -1);
      response.put("message", "fail: " + e.getMessage());
      response.set("data", null);
      return response;
    }
  }

  @Override
  public ObjectNode getOrderById(long orderId) {
    ObjectNode response = objectMapper.createObjectNode();
    try {
      PaymentLinkData order = payOS.getPaymentLinkInformation(orderId);

      response.set("data", objectMapper.valueToTree(order));
      response.put("error", 0);
      response.put("message", "ok");
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
  public ObjectNode cancelOrder(int orderId) {
    ObjectNode response = objectMapper.createObjectNode();
    try {
      PaymentLinkData order = payOS.cancelPaymentLink(orderId, null);
      response.set("data", objectMapper.valueToTree(order));
      response.put("error", 0);
      response.put("message", "ok");
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
  public ObjectNode confirmWebhook(ConfirmWebhookRequest requestBody) {
    ObjectNode response = objectMapper.createObjectNode();
    try {
      String str = payOS.confirmWebhook(requestBody.getWebhookUrl());
      response.set("data", objectMapper.valueToTree(str));
      response.put("error", 0);
      response.put("message", "ok");
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
