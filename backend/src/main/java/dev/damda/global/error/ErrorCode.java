package dev.damda.global.error;

import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;

public interface ErrorCode {
    String name();

    HttpStatus status();

    String message();

    Level logLevel();
}
