package dev.damda.global.error;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetail> handleBusiness(BusinessException e) {
        return respond(e.getErrorCode(), e);
    }

    private ResponseEntity<ProblemDetail> respond(ErrorCode errorCode, Exception e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(errorCode.status(), errorCode.message());
        problem.setProperty("code", errorCode.name());
        log.atLevel(errorCode.logLevel()).log("{}: {}", errorCode.name(), e.getMessage());
        return ResponseEntity.status(errorCode.status()).body(problem);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> handleAuthentication(AuthenticationException e) {
        return respond(CommonErrorCode.UNAUTHENTICATED, e);
    }

    @ExceptionHandler(CsrfException.class)
    public ResponseEntity<ProblemDetail> handleCsrf(CsrfException e) {
        return respond(CommonErrorCode.CSRF_TOKEN_INVALID, e);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException e) {
        return respond(CommonErrorCode.ACCESS_DENIED, e);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleException(Exception e) {
        ErrorCode errorCode = CommonErrorCode.INTERNAL_ERROR;
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(errorCode.status(), errorCode.message());
        problem.setProperty("code", errorCode.name());
        log.error("처리하지 못한 예외", e);
        return ResponseEntity.status(errorCode.status()).body(problem);
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleExceptionInternal(
            Exception ex, @Nullable Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        ErrorCode errorCode = resolveErrorCode(ex, statusCode);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(statusCode, errorCode.message());
        problem.setProperty("code", errorCode.name());

        if (ex instanceof MethodArgumentNotValidException manv) {
            problem.setProperty(
                    "errors",
                    manv.getBindingResult().getFieldErrors().stream()
                            .map(fieldError ->
                                    new FieldErrorResponse(fieldError.getField(), fieldError.getDefaultMessage()))
                            .toList());
        }

        if (statusCode.is5xxServerError()) {
            log.error("처리하지 못한 예외", ex);
        } else {
            log.atLevel(errorCode.logLevel()).log("{}: {}", errorCode.name(), ex.getMessage());
        }

        return super.handleExceptionInternal(ex, problem, headers, statusCode, request);
    }

    private ErrorCode resolveErrorCode(Exception ex, HttpStatusCode statusCode) {
        if (ex instanceof MethodArgumentNotValidException) {
            return CommonErrorCode.VALIDATION_FAILED;
        }
        if (statusCode.is5xxServerError()) {
            return CommonErrorCode.INTERNAL_ERROR;
        }
        return switch (statusCode.value()) {
            case 404 -> CommonErrorCode.NOT_FOUND;
            case 405 -> CommonErrorCode.METHOD_NOT_ALLOWED;
            default -> CommonErrorCode.INVALID_REQUEST;
        };
    }
}
