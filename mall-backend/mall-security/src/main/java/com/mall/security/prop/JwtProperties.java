package com.mall.security.prop;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "mall.jwt")
public class JwtProperties {
    private String secret = "MallDevSecretKeyMustBe256BitsLongMallDevSecretKeyMustBe256Bits";
    private long accessExpireMs = 30 * 60_000L;
    private long refreshExpireMs = 7 * 24 * 60 * 60_000L;
}
