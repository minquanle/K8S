package com.example.demo.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;

@Component
@Profile("!noauth")
public class ClientSecretLogger {
    private static final Logger log = LoggerFactory.getLogger(ClientSecretLogger.class);

    @Value("${spring.security.oauth2.client.registration.auth0.client-secret:changeme}")
    private String clientSecret;

    @PostConstruct
    void check() {
        if (clientSecret == null || "changeme".equals(clientSecret) || clientSecret.startsWith("REPLACE_")) {
            log.error("Auth0 client secret not configured. Set ENV AUTH0_CLIENT_SECRET.");
        } else {
            log.info("Auth0 client secret length = {} (hidden)", clientSecret.length());
        }
    }
}
