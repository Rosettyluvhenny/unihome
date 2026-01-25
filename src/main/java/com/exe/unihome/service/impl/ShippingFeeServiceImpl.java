package com.exe.unihome.service.impl;

import com.exe.unihome.common.exception.AppException;
import com.exe.unihome.common.exception.ErrorCode;
import com.exe.unihome.config.ShippingProperties;
import com.exe.unihome.persistence.entity.shipping.ShippingPriceDistance;
import com.exe.unihome.persistence.repository.ShippingPriceDistanceRepository;
import com.exe.unihome.service.ShippingFeeService;
import com.exe.unihome.service.model.ShippingFeeResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class ShippingFeeServiceImpl implements ShippingFeeService {

    private final ShippingPriceDistanceRepository shippingPriceDistanceRepository;
    private final ShippingProperties shippingProperties;

    @Override
    public ShippingFeeResult calculate(double distanceKm, BigDecimal subtotal) {
        double normalizedDistance = Math.max(0d, distanceKm);
        BigDecimal nonNullSubtotal = subtotal != null ? subtotal : BigDecimal.ZERO;

        boolean freeApplied = isFreeShipping(nonNullSubtotal);
        if (freeApplied) {
            return ShippingFeeResult.builder()
                .shippingFee(BigDecimal.ZERO)
                .freeApplied(true)
                .distanceKm(normalizedDistance)
                .build();
        }

        int distanceForTier = (int) Math.ceil(normalizedDistance);
        ShippingPriceDistance tier = shippingPriceDistanceRepository.findTiersForDistance(distanceForTier).stream()
            .findFirst()
            .orElseGet(() -> shippingPriceDistanceRepository.findFirstByMaxDistanceKmIsNullOrderByMinDistanceKmAsc()
                .orElseThrow(() -> new AppException(ErrorCode.SHIPPING_TIER_NOT_FOUND)));

        BigDecimal fee = tier.getPrice() != null ? tier.getPrice() : BigDecimal.ZERO;
        if (tier.getMaxDistanceKm() == null && tier.getPricePerKm() != null) {
            double extraDistance = Math.max(0, normalizedDistance - tier.getMinDistanceKm());
            BigDecimal extraCost = tier.getPricePerKm()
                .multiply(BigDecimal.valueOf(extraDistance))
                .setScale(0, RoundingMode.CEILING);
            fee = fee.add(extraCost);
        }

        return ShippingFeeResult.builder()
            .shippingFee(fee)
            .freeApplied(false)
            .distanceKm(normalizedDistance)
            .build();
    }

    private boolean isFreeShipping(BigDecimal subtotal) {
        BigDecimal freeThreshold = shippingProperties.getFreeThreshold();
        return freeThreshold != null && subtotal.compareTo(freeThreshold) >= 0;
    }
}
