package chimhaha.chimcard.exception;

import chimhaha.chimcard.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> notFound(ResourceNotFoundException e) {
        return ResponseEntity.status(NOT_FOUND).body(ApiResponse.notFound(e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<String>> badRequest(IllegalArgumentException e) {
        return ResponseEntity.status(BAD_REQUEST).body(ApiResponse.badRequest(e.getMessage()));
    }

    //JwtAuthenticationEntryPoint 에서 발생하는 AuthenticationException 처리도 함께 하도록 변경
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<String>> invalidToken(AuthenticationException e) {
        return ResponseEntity.status(UNAUTHORIZED).body(ApiResponse.unAuthorized(e.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<String>> accessDeniedException(AccessDeniedException e) {
        return ResponseEntity.status(FORBIDDEN).body(ApiResponse.forbidden(e.getMessage()));
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<String>> optimisticLockException(OptimisticLockingFailureException e) {
        log.error(e.getMessage(), e);
        return ResponseEntity.status(CONFLICT).body(ApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> internalServerError(Exception e) {
        log.error(e.getMessage(), e);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
    }
}
