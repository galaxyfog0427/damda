package dev.damda.global.error;

import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;

public enum CommonErrorCode implements ErrorCode {
    NOT_IMPLEMENTED(HttpStatus.NOT_IMPLEMENTED, "아직 준비 중인 기능입니다.", Level.WARN),
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "잠시 후 다시 시도해 주세요.", Level.INFO),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "잠시 후 다시 시도해 주세요.", Level.ERROR);

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
