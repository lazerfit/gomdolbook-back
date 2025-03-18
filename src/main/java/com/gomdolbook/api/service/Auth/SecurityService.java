package com.gomdolbook.api.service.Auth;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class SecurityService {

    public String getUserEmailFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt principal = (Jwt) authentication.getPrincipal();
        return principal.getClaim("email");
    }

    public String getCacheKey(String value) {
        return getUserEmailFromSecurityContext() + ":" + Arrays.toString(new Object[]{value});
    }
}
