package dev.damda.global.error;

import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;

public enum CommonErrorCode implements ErrorCode {
    NOT_IMPLEMENTED(HttpStatus.NOT_IMPLEMENTED, "아직 준비 중인 기능입니다.", Level.WARN),
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "잠시 후 다시 시도해 주세요.", Level.INFO),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "잠시 후 다시 시도해 주세요.", Level.ERROR),
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.", Level.INFO),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 필요합니다.", Level.WARN),
    CSRF_TOKEN_INVALID(HttpStatus.FORBIDDEN, "요청이 만료되었습니다. 다시 시도해 주세요.", Level.WARN),
    NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 주소를 찾을 수 없습니다.", Level.INFO),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 요청 방식입니다.", Level.INFO),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않습니다.", Level.INFO),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "입력값을 확인해 주세요.", Level.INFO);

    private final HttpStatus status;
    private final String message;
    private final Level logLevel;

    CommonErrorCode(HttpStatus status, String message, Level logLevel) {
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
