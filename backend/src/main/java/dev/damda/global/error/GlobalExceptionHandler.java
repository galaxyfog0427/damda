package dev.damda.global.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetail> handleBusiness(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(errorCode.status(), errorCode.message());
        problem.setProperty("code", errorCode.name());
        log.atLevel(errorCode.logLevel()).log("{}: {}", errorCode.name(), e.getMessage());
        return ResponseEntity.status(errorCode.status()).body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleException(Exception e) {
        ErrorCode errorCode = CommonErrorCode.INTERNAL_ERROR;
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(errorCode.status(), errorCode.message());
        problem.setProperty("code", errorCode.name());
        log.error("처리하지 못한 예외", e);
        return ResponseEntity.status(errorCode.status()).body(problem);
    }
}
