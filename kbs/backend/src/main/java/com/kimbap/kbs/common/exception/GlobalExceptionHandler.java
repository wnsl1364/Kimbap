package com.kimbap.kbs.common.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  // @ExceptionHandler(InsufficientStockException.class)
  // public ResponseEntity<?> handleInsufficientStock(InsufficientStockException e) {
  //     return ResponseEntity.status(HttpStatus.BAD_REQUEST)
  //                          .body(Map.of("error", e.getMessage())); // {"error": "자재 재고 부족: MAT-1001"}
  // }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> handleGeneral(Exception e) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                         .body(Map.of("message", "서버 오류 발생: " + e.getMessage()));
  }
  @ExceptionHandler(InsufficientStockException.class)
  public ResponseEntity<?> handleStock(InsufficientStockException e) {
    return ResponseEntity.badRequest()
                         .body(Map.of("message", e.getMessage()));
  }
}