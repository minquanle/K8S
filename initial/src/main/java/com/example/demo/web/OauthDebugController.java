package com.example.demo.web;

import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@Profile("!noauth")
public class OauthDebugController {

    private final ClientRegistrationRepository repo;

    public OauthDebugController(ClientRegistrationRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/debug/client")
    public Map<String,Object> client() {
        var inMem = (org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository) repo;
        ClientRegistration reg = inMem.findByRegistrationId("auth0");
        Map<String,Object> m = new HashMap<>();
        if (reg == null) {
            m.put("error","registration 'auth0' not found");
            return m;
        }
        m.put("registrationId", reg.getRegistrationId());
        m.put("clientId", reg.getClientId());
        m.put("redirectUri", reg.getRedirectUri());
        m.put("scopes", reg.getScopes());
        m.put("providerDetailsIssuer", reg.getProviderDetails().getIssuerUri());
        return m;
    }
}
