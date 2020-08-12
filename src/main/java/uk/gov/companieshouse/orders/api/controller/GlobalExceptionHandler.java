package uk.gov.companieshouse.orders.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.companieshouse.orders.api.exception.MongoOperationException;
import uk.gov.companieshouse.orders.api.model.ApiError;
import uk.gov.companieshouse.orders.api.util.FieldNameConverter;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final FieldNameConverter converter;

    public GlobalExceptionHandler(FieldNameConverter converter) {
        this.converter = converter;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            final MethodArgumentNotValidException ex,
            final HttpHeaders headers,
            final HttpStatus status,
            final WebRequest request) {
        final ApiError apiError = buildBadRequestApiError(ex);
        return handleExceptionInternal(ex, apiError, headers, apiError.getStatus(), request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            final HttpMessageNotReadableException ex,
            final HttpHeaders headers,
            final HttpStatus status,
            final WebRequest request) {

        if (ex.getCause() instanceof JsonProcessingException) {
            final ApiError apiError = buildBadRequestApiError((JsonProcessingException) ex.getCause());
            return handleExceptionInternal(ex, apiError, headers, apiError.getStatus(), request);
        }

        return super.handleHttpMessageNotReadable(ex, headers, status, request);
    }

    /**
     * Returns Http Status 500 when mongo db fails to run an operation.
     * @param ex exception
     * @return
     */
    @ExceptionHandler(MongoOperationException.class)
    public ResponseEntity<Object> handleMongoOperationException(final MongoOperationException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

    /**
     * Utility to build ApiError from MethodArgumentNotValidException.
     *
     * @param ex the MethodArgumentNotValidException handled
     * @return the resulting ApiError
     */
    ApiError buildBadRequestApiError(final MethodArgumentNotValidException ex) {
        final List<String> errors = new ArrayList<>();

        for (final FieldError error : ex.getBindingResult().getFieldErrors()) {
            System.out.println("HIII");
            System.out.println(error.getObjectName());

            errors.add(converter.toSnakeCase(error.getField()) + ": " + error.getDefaultMessage());
        }
        for (final ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            errors.add(error.getObjectName() + ": " + error.getDefaultMessage());
        }

        return new ApiError(HttpStatus.BAD_REQUEST, errors);
    }

    /**
     * Utility to build ApiError from JsonProcessingException.
     *
     * @param jpe the JsonProcessingException handled
     * @return the resulting ApiError
     */
    ApiError buildBadRequestApiError(final JsonProcessingException jpe) {
        final String errorMessage = jpe.getOriginalMessage();
        return new ApiError(HttpStatus.BAD_REQUEST, singletonList(errorMessage));
    }

}