package com.example.demo.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Convert Keycloak realm roles (realm_access.roles) to Spring Security authorities with ROLE_ prefix.
 */
public class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    @Override
    public Collection<GrantedAuthority> convert(Jwt source) {
        Map<String, Object> realmAccess = source.getClaim("realm_access");
        if (realmAccess == null) return Collections.emptyList();
        Object rolesObj = realmAccess.get("roles");
        if (!(rolesObj instanceof List<?> list)) return Collections.emptyList();
        return list.stream()
                .filter(r -> r instanceof String)
                .map(r -> (String) r)
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r.toUpperCase())
                .distinct()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}

