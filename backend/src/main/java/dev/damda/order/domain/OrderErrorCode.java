package dev.damda.order.domain;

import dev.damda.global.error.ErrorCode;
import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;

public enum OrderErrorCode implements ErrorCode {
    TEMPLATE_NOT_DOWNLOADABLE(HttpStatus.CONFLICT, "아직 받을 수 없는 템플릿입니다.", Level.INFO),
    DROP_ONLY_TEMPLATE(HttpStatus.CONFLICT, "드롭으로만 받을 수 있는 템플릿입니다.", Level.INFO),
    LIBRARY_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "라이브러리에 없는 템플릿입니다.", Level.INFO);

    private final HttpStatus status;
    private final String message;
    private final Level logLevel;

    OrderErrorCode(HttpStatus status, String message, Level logLevel) {
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
