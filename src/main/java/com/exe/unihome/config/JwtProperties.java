package com.exe.unihome.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String signerKey;
    private long validDuration;
    private long refreshableDuration;
}
