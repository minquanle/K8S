package com.example.demo.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class SecretStatusController {

    @Value("${spring.security.oauth2.client.registration.auth0.client-secret:changeme}")
    private String secret;

    @GetMapping("/debug/secret")
    public Map<String,Object> secretStatus() {
        boolean placeholder = "changeme".equals(secret) || secret.startsWith("REPLACE_");
        return Map.of(
                "placeholder", placeholder,
                "length", secret == null ? 0 : secret.length(),
                "message", placeholder ? "Auth0 client secret NOT set. Set ENV AUTH0_CLIENT_SECRET or edit application.properties." : "Auth0 client secret is loaded."
        );
    }
}
