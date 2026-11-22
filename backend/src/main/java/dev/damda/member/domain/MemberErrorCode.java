package dev.damda.member.domain;

import dev.damda.global.error.ErrorCode;
import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;

public enum MemberErrorCode implements ErrorCode {
    INVALID_SIGNUP_CODE(HttpStatus.BAD_REQUEST, "인증 코드가 맞지 않거나 만료되었습니다.", Level.INFO),
    INVALID_SIGNUP_TOKEN(HttpStatus.BAD_REQUEST, "가입 확인이 만료되었습니다. 처음부터 다시 진행해 주세요.", Level.INFO),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "비밀번호가 맞지 않습니다.", Level.INFO),
    WITHDRAWAL_NOT_ALLOWED(HttpStatus.FORBIDDEN, "이 계정은 탈퇴할 수 없습니다.", Level.INFO),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 맞지 않습니다.", Level.INFO);

    private final HttpStatus status;
    private final String message;
    private final Level logLevel;

    MemberErrorCode(HttpStatus status, String message, Level logLevel) {
        this.status = status;
        this.message = message;
        this.logLevel = logLevel;
    }

    @Override
    public HttpStatus status() {
        return status;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public Level logLevel() {
        return logLevel;
    }
}
