package com.gomdolbook.api.aop;

import com.gomdolbook.api.errors.UserValidationError;
import com.gomdolbook.api.persistence.entity.User;
import com.gomdolbook.api.persistence.entity.User.Role;
import com.gomdolbook.api.service.Auth.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Aspect
@Component
public class EmailUserValidationAspect {

    private final UserService userService;

    @Before("@annotation(com.gomdolbook.api.config.annotations.UserCheckAndSave)")
    public void doValidation() {
        try {
            if (!isValidated()) {
                Jwt principal = getPrincipal();
                String email = principal.getClaim("email");
                User user = new User(email, "img", Role.USER);
                userService.addUser(user);
            }
        } catch (Exception e) {
            log.warn(e.getMessage());
            throw new UserValidationError("user validation failed", e);
        }
    }

    private boolean isValidated() {
        Jwt principal = getPrincipal();
        String securityContextEmail = principal.getClaim("email");
        return userService.findByEmail(securityContextEmail).isPresent();
    }

    private Jwt getPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (Jwt) authentication.getPrincipal();
    }
}
