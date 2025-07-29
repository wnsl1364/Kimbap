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
import org.springframework.web.bind.annotation.RequestParam;
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
    public Map<String, Object> getOrderList(
        @RequestParam(required = false) String id,
        @RequestParam(required = false) String memType
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> params = new HashMap<>();
            // 매출업체일 경우 → id 기준 필터링
            if ("p2".equals(memType)) {
                params.put("cpCd", id);  // 뷰에서 cp_cd가 주문자 ID로 추정
            }
            List<OrderVO> list = orderService.getOrderList(params);
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

    // 주문 상세 조회 (주문코드로 단건 조회)
    @GetMapping("/{ordCd}")
    public ResponseEntity<?> getOrderByOrdCd(@PathVariable String ordCd) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 주문 기본 정보 + 상세 목록 포함해서 받아오기
            OrderVO order = orderService.getOrderWithDetails(ordCd);

            response.put("result_code", "SUCCESS");
            response.put("message", "주문 단건 조회 성공");
            response.put("data", order);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("result_code", "FAIL");
            response.put("message", "주문 단건 조회 실패");
            response.put("data", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 주문 수정 (주문상세 전체 삭제 후 재등록)
    @PutMapping("/{ordCd}/update")
    public ResponseEntity<?> updateOrder(@PathVariable String ordCd, @RequestBody OrderVO orderVO) {
        Map<String, Object> response = new HashMap<>();
        try {
            orderVO.setOrdCd(ordCd); // path variable을 VO에 세팅

            orderService.updateOrder(orderVO);

            response.put("result_code", "SUCCESS");
            response.put("message", "주문 수정 성공");
            response.put("data", Map.of("ordCd", ordCd));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("result_code", "FAIL");
            response.put("message", "주문 수정 실패");
            response.put("data", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
