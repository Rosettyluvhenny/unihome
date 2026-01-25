package com.exe.unihome.service;

import com.exe.unihome.service.model.ShippingFeeResult;

import java.math.BigDecimal;

public interface ShippingFeeService {

    ShippingFeeResult calculate(double distanceKm, BigDecimal subtotal);
}
