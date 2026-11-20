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
@RequestMapping("/api/v1")
public class SignupController {

    @PostMapping("/signup/codes")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void sendSignupCode(@RequestBody @Valid SignupCodeRequest request) {
        throw new BusinessException(CommonErrorCode.NOT_IMPLEMENTED);
    }

    @PostMapping("/signup/verifications")
    public SignupVerificationResponse verifySignupCode(@RequestBody @Valid SignupVerificationRequest request) {
        throw new BusinessException(CommonErrorCode.NOT_IMPLEMENTED);
    }

    @PostMapping("/members")
    @ResponseStatus(HttpStatus.CREATED)
    public void signup(@RequestBody @Valid SignupRequest request) {
        throw new BusinessException(CommonErrorCode.NOT_IMPLEMENTED);
    }
}
