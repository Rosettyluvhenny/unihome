package com.exe.unihome.service.model;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class ShippingFeeResult {

    BigDecimal shippingFee;
    boolean freeApplied;
    double distanceKm;
}
