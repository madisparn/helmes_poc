package org.bashhead.helmes.web;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(CustomExceptionHandler.class);

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
    LOG.warn("Request not valid: {}", ex.getMessage());

    var bindingResult = ex.getBindingResult();
    var errors =  new ArrayList<Map<String, String>>();
    for (var fieldError : bindingResult.getFieldErrors()) {
      errors.add(Map.of("field", fieldError.getField(), "message", fieldError.getDefaultMessage()));
    }
    for (var objectError : bindingResult.getGlobalErrors()) {
      errors.add(Map.of("message", objectError.getDefaultMessage()));
    }

    var body = ProblemDetail.forStatusAndDetail(status, "Validation failed");
    body.setProperty("errors", errors);
    return handleExceptionInternal(ex, body, headers, status, request);
  }
}
