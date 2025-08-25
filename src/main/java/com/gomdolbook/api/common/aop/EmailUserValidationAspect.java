package com.gomdolbook.api.common.aop;

import com.gomdolbook.api.domain.shared.UserValidationException;
import com.gomdolbook.api.domain.models.user.User;
import com.gomdolbook.api.domain.models.user.User.Role;
import com.gomdolbook.api.domain.services.SecurityService;
import com.gomdolbook.api.application.user.UserApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Aspect
@Component
public class EmailUserValidationAspect {

    private final UserApplicationService userApplicationService;
    private final SecurityService securityService;

    @Before("@annotation(com.gomdolbook.api.common.config.annotations.UserCheckAndSave)")
    public void doValidation() {
        try {
            if (!isValidated()) {
                String email = securityService.getUserEmailFromSecurityContext();
                User user = new User(email, "img", Role.USER);
                userApplicationService.addUser(user);
            }
        } catch (Exception e) {
            log.warn(e.getMessage());
            throw new UserValidationException("user validation failed", e);
        }
    }

    private boolean isValidated() {
        String email = securityService.getUserEmailFromSecurityContext();
        return userApplicationService.find(email).isPresent();
    }

}
