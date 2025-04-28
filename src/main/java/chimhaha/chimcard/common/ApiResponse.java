package chimhaha.chimcard.common;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

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

    public static <T> ApiResponse<T> error(T data) {
        return new ApiResponse<>(INTERNAL_SERVER_ERROR.value(),
                INTERNAL_SERVER_ERROR.getReasonPhrase(), now(), data);
    }
}
