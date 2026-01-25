package com.exe.unihome.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.warehouse")
public class WarehouseProperties {

    private String address;
    private Double latitude;
    private Double longitude;
}
