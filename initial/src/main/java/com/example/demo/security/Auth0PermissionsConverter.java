package com.example.demo.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Converts Auth0 permissions claim to Spring GrantedAuthorities.
 * - Each permission string becomes PERM_<UPPER>
 * - Adds ROLE_ADMIN if any create/update/delete permission present
 * - Adds ROLE_USER if any read permission present
 */
@Component
public class Auth0PermissionsConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final String claimName;

    public Auth0PermissionsConverter(@Value("${app.auth0.roles-claim:permissions}") String claimName) {
        this.claimName = claimName;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Object raw = jwt.getClaim(claimName);
        List<String> perms;
        if (raw instanceof Collection<?> coll) {
            perms = coll.stream().filter(o -> o instanceof String).map(Object::toString).toList();
        } else {
            perms = Collections.emptyList();
        }
        Set<String> authorities = new LinkedHashSet<>();
        boolean anyWrite = false;
        boolean anyRead = false;
        for (String p : perms) {
            String norm = p.trim();
            if (norm.isEmpty()) continue;
            authorities.add("PERM_" + norm.toUpperCase().replace(':', '_'));
            if (norm.startsWith("read:")) anyRead = true;
            if (norm.startsWith("create:") || norm.startsWith("update:") || norm.startsWith("delete:")) anyWrite = true;
        }
        if (anyRead) authorities.add("ROLE_USER");
        if (anyWrite) authorities.add("ROLE_ADMIN");
        return authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }
}

