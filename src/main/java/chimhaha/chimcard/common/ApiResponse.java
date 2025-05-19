package chimhaha.chimcard.common;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;
import static org.springframework.http.HttpStatus.*;

public record ApiResponse<T>(int status,
                             String message,
                             LocalDateTime time,
                             T data) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(OK.value(), OK.getReasonPhrase(), now(), data);
    }

    public static <T> ApiResponse<T> notFound(T data) {
        return new ApiResponse<>(NOT_FOUND.value(), NOT_FOUND.getReasonPhrase(), now(), data);
    }

    public static <T> ApiResponse<T> badRequest(T data) {
        return new ApiResponse<>(BAD_REQUEST.value(), BAD_REQUEST.getReasonPhrase(), now(), data);
    }

    public static <T> ApiResponse<T> unAuthorized(T data) {
        return new ApiResponse<>(UNAUTHORIZED.value(), UNAUTHORIZED.getReasonPhrase(), now(), data);
    }

    public static <T> ApiResponse<T> forbidden(T data) {
        return new ApiResponse<>(FORBIDDEN.value(), FORBIDDEN.getReasonPhrase(), now(), data);
    }

    public static <T> ApiResponse<T> error(T data) {
        return new ApiResponse<>(INTERNAL_SERVER_ERROR.value(),
                INTERNAL_SERVER_ERROR.getReasonPhrase(), now(), data);
    }
}
