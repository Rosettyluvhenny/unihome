package com.exe.unihome.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Data
@Component
@ConfigurationProperties(prefix = "app.shipping")
public class ShippingProperties {

    private BigDecimal freeThreshold;
}
