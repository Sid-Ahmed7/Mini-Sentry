package com.sentry.backend.exceptions;

import java.util.Map;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.sentry.backend.common.CurrentLocale;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
@AllArgsConstructor
public class GlobalException {

    private final MessageSource messageSource;

    @ExceptionHandler(ApiException.class)
    ResponseEntity<Map<String, String>> api(ApiException exception) {
        return ResponseEntity.status(exception.status()).body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    ResponseEntity<Map<String, String>> badRequest(Exception exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", messageSource.getMessage("error.invalidRequest", null, CurrentLocale.getCurrentLocale())));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<Map<String, String>> notFound(Exception exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", messageSource.getMessage("error.notFound", null, CurrentLocale.getCurrentLocale())));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<Map<String, String>> method(Exception exception) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(Map.of("error", messageSource.getMessage("error.methodNotAllowed", null, CurrentLocale.getCurrentLocale())));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ResponseEntity<Map<String, String>> mediaType(Exception exception) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(Map.of("error", messageSource.getMessage("error.unsupportedMediaType", null, CurrentLocale.getCurrentLocale())));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, String>> fallback(Exception exception) {
        log.error("Unexpected error: {}", exception.getClass().getName(), exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", messageSource.getMessage("error.internalError", null, CurrentLocale.getCurrentLocale())));
    }
}