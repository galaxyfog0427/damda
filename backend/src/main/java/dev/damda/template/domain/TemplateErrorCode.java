package dev.damda.template.domain;

import dev.damda.global.error.ErrorCode;
import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;

public enum TemplateErrorCode implements ErrorCode {
    TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "템플릿을 찾을 수 없습니다.", Level.INFO);

    private final HttpStatus status;
    private final String message;
    private final Level logLevel;

    TemplateErrorCode(HttpStatus status, String message, Level logLevel) {
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
