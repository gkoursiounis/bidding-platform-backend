package com.Auctions.backEnd.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "app")
@Component
public class AppConfig {
    private String secret;

    private long tokenValidityInSeconds;

    public synchronized String getSecret() {
        return this.secret;
    }

    public synchronized void setSecret(String secret) {
        this.secret = secret;
    }

    public synchronized long getTokenValidityInSeconds() {
        return this.tokenValidityInSeconds;
    }

    public synchronized void setTokenValidityInSeconds(long tokenValidityInSeconds) {
        this.tokenValidityInSeconds = tokenValidityInSeconds;
    }

}