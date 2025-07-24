package com.kimbap.kbs.order.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kimbap.kbs.order.service.OrderService;
import com.kimbap.kbs.order.service.OrderVO;

import lombok.RequiredArgsConstructor;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {
  private final OrderService orderService;

  // 주문 등록
  @PostMapping("/register")
  public ResponseEntity<Map<String, Object>> registerOrder(@RequestBody OrderVO orderVO) {
      Map<String, Object> response = new HashMap<>();
      try {
          orderService.registerOrder(orderVO);

          response.put("result_code", "SUCCESS");
          response.put("message", "주문 등록 성공");
          response.put("data", orderVO);
          return ResponseEntity.ok(response);

      } catch (Exception e) {
          response.put("result_code", "FAIL");
          response.put("message", "주문 등록 실패");
          response.put("data", e.getMessage());
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
      }
  }



  // 주문 목록 조회
  @GetMapping("/list")
  public Map<String, Object> getOrderList() {
      Map<String, Object> response = new HashMap<>();
      try {
          List<OrderVO> list = orderService.getOrderList();
          response.put("result_code", "SUCCESS");
          response.put("message", "주문 목록 조회 성공");
          response.put("data", list);
      } catch (Exception e) {
          response.put("result_code", "FAIL");
          response.put("message", "주문 목록 조회 실패");
          response.put("data", e.getMessage());
      }
      return response;
  }

}
