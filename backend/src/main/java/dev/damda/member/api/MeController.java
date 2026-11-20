package dev.damda.member.api;

import dev.damda.global.error.BusinessException;
import dev.damda.global.error.CommonErrorCode;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
public class MeController {

    @GetMapping
    public MeResponse me() {
        throw new BusinessException(CommonErrorCode.NOT_IMPLEMENTED);
    }

    @PostMapping("/withdrawal")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdraw(@RequestBody @Valid WithdrawalRequest request) {
        throw new BusinessException(CommonErrorCode.NOT_IMPLEMENTED);
    }
}
