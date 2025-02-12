package com.gomdolbook.api.aop;

import static com.gomdolbook.api.utils.SecurityUtil.getUserEmailFromSecurityContext;

import com.gomdolbook.api.errors.UserValidationError;
import com.gomdolbook.api.persistence.entity.User;
import com.gomdolbook.api.persistence.entity.User.Role;
import com.gomdolbook.api.service.Auth.UserService;
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

    private final UserService userService;

    @Before("@annotation(com.gomdolbook.api.config.annotations.UserCheckAndSave)")
    public void doValidation() {
        try {
            if (!isValidated()) {
                String email = getUserEmailFromSecurityContext();
                User user = new User(email, "img", Role.USER);
                userService.addUser(user);
            }
        } catch (Exception e) {
            log.warn(e.getMessage());
            throw new UserValidationError("user validation failed", e);
        }
    }

    private boolean isValidated() {
        String email = getUserEmailFromSecurityContext();
        return userService.findByEmail(email).isPresent();
    }

}
