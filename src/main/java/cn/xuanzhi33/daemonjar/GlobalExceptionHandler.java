package cn.xuanzhi33.daemonjar;

import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result> handleException(Exception e) {
        if (e instanceof NoResourceFoundException) {
            return getResponseEntity(404, e);
        } else if (e instanceof MissingServletRequestParameterException || e instanceof BadRequestException) {
            return getResponseEntity(400, e);
        } else {
            return getResponseEntity(500, e);
        }
    }


    private static ResponseEntity<Result> getResponseEntity(int code, Exception e) {
        return ResponseEntity.status(code).body(Result.error(code, e.getMessage()));
    }
}
