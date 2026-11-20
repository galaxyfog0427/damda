package dev.damda.member.api;

import dev.damda.global.error.BusinessException;
import dev.damda.global.error.CommonErrorCode;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/password-resets")
public class PasswordResetController {

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void requestPasswordReset(@RequestBody @Valid PasswordResetRequest request) {
        throw new BusinessException(CommonErrorCode.NOT_IMPLEMENTED);
    }
}
