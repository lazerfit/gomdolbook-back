package com.gomdolbook.api.aop;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ValidateUserWithEmailAspect {

    @Before("@annotation(com.gomdolbook.api.config.annotations.PreAuthorizeWithEmail)")
    public void doPreAuthorize(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();

        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        Pattern pattern = Pattern.compile(regex);

        String validEmail = Arrays.stream(args)
            .filter(String.class::isInstance)
            .filter(arg -> pattern.matcher((String) arg).matches())
            .findFirst()
            .map(String::valueOf)
            .orElseThrow(NoSuchElementException::new);

        if (!isValidate(validEmail)) {
            throw new AuthorizationDeniedException("Access Denied");
        }
    }

    private boolean isValidate(String email) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt principal = (Jwt) authentication.getPrincipal();
        String securityContextEmail = principal.getClaim("email");

        return securityContextEmail.equals(email);
    }
}
