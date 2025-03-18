package com.gomdolbook.api.config;

import com.gomdolbook.api.service.Auth.SecurityService;
import java.lang.reflect.Method;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.interceptor.KeyGenerator;

@RequiredArgsConstructor
public class CustomKeyGenerator implements KeyGenerator {

    private final SecurityService securityService;

    @Override
    public Object generate(Object target, Method method, Object... params) {
        String email = securityService.getUserEmailFromSecurityContext();

        return email + ":" + Arrays.toString(params);
    }
}
