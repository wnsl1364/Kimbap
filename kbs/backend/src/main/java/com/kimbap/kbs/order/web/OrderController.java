package com.kimbap.kbs.order.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kimbap.kbs.order.service.OrderService;
import com.kimbap.kbs.order.service.OrderVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
          log.error("주문 등록 중 오류 발생", e);
          response.put("result_code", "FAIL");
          response.put("message", "주문 등록 실패");
          response.put("data", e.getMessage());
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
      }
  }

  // 주문 삭제 (비활성화)
    @PutMapping("/order/{ordCd}/deactivate")
    public ResponseEntity<?> deactivateOrder(@PathVariable String ordCd) {
        try {
            orderService.deactivateOrder(ordCd);
            return ResponseEntity.ok(Map.of(
                "result_code", "SUCCESS",
                "message", "삭제 성공"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "result_code", "FAIL",
                "message", "삭제 실패"
            ));
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
