package com.gomdolbook.api.aop;

import com.gomdolbook.api.persistence.entity.User;
import com.gomdolbook.api.persistence.entity.User.Role;
import com.gomdolbook.api.service.Auth.UserService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Aspect
@Component
public class EmailUserValidationAspect {

    private final UserService userService;

    @Before("@annotation(com.gomdolbook.api.config.annotations.UserCheckAndSave)")
    public void doValidation() {
        if (!isValidated()) {
            Jwt principal = getPrincipal();
            String email = principal.getClaim("email");
            User user = new User(email, "img", Role.USER);
            userService.addUser(user);
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
