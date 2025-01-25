package com.gomdolbook.api.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class CustomSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        List<SimpleGrantedAuthority> authorities = Arrays.stream(annotation.roles())
            .map(role -> "ROLE_" + role)
            .map(SimpleGrantedAuthority::new).toList();

        Jwt jwt = Jwt.withTokenValue("mock-token")
            .headers(header -> {
                header.put("alg", "RS256");
                header.put("typ", "JWT");
                header.put("kid", "mock-kid");
            })
            .claim("email", annotation.email())
            .claim("authorities", "user")
            .claim("scope", "user")
            .build();

        JwtAuthenticationToken authToken = new JwtAuthenticationToken(jwt,
            authorities);
        context.setAuthentication(authToken);

        return context;
    }
}
