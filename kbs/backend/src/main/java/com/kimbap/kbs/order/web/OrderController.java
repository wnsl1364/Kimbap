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
    @PutMapping("/{ordCd}/deactivate")
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
        @RequestParam(required = false) String memType,
        @RequestParam(required = false) String ordCd,
        @RequestParam(required = false) String ordDtStart,
        @RequestParam(required = false) String ordDtEnd,
        @RequestParam(required = false) String deliReqDtStart,
        @RequestParam(required = false) String deliReqDtEnd,
        @RequestParam(required = false) String cpName,
        @RequestParam(required = false) String ordStatus,
        @RequestParam(required = false) String cpCd
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> params = new HashMap<>();

            // 회원 유형 분기 처리
            if ("p2".equals(memType) && cpCd != null && !cpCd.isEmpty()) {
                params.put("cpCd", cpCd);
            }

            // 검색 조건 추가
            if (ordCd != null && !ordCd.isEmpty()) {
                params.put("ordCd", ordCd);
            }
            if (cpName != null && !cpName.isEmpty()) {
                params.put("cpName", cpName);
            }
            if (ordStatus != null && !ordStatus.isEmpty()) {
                if ("p2".equals(memType)) {
                    params.put("ordStatusCustomer", ordStatus); // 매출업체
                } else {
                    params.put("ordStatusInternal", ordStatus); // 내부직원
                }
            }


            // 날짜 범위 분리
            if (ordDtStart != null && !ordDtStart.isEmpty()) {
                params.put("ordDtStart", ordDtStart);
            }
            if (ordDtEnd != null && !ordDtEnd.isEmpty()) {
                params.put("ordDtEnd", ordDtEnd);
            }
            if (deliReqDtStart != null && !deliReqDtStart.isEmpty()) {
                params.put("deliReqDtStart", deliReqDtStart);
            }
            if (deliReqDtEnd != null && !deliReqDtEnd.isEmpty()) {
                params.put("deliReqDtEnd", deliReqDtEnd);
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

    // 주문 승인(내부직원)
    @PutMapping("/{ordCd}/approve")
    public ResponseEntity<?> approveOrder(@PathVariable String ordCd, @RequestBody OrderVO orderVO) {
        try {
            // 주문번호 세팅
            orderVO.setOrdCd(ordCd);

            // 납기 가능일자 누락 검증
            boolean hasMissingDeliAvail = orderVO.getOrderDetails().stream()
                .anyMatch(d -> d.getDeliAvailDt() == null);
            if (hasMissingDeliAvail) {
                return ResponseEntity.badRequest().body(Map.of(
                    "result_code", "FAIL",
                    "message", "모든 제품에 납기 가능일자를 입력해야 승인할 수 있습니다."
                ));
            }

            // 상태 변경
            orderVO.setOrdStatusInternal("a2"); // 내부: 승인
            orderVO.setOrdStatusCustomer("s2"); // 외부: 접수완료

            // DB에 업데이트
            orderService.updateOrder(orderVO);

            return ResponseEntity.ok(Map.of(
                "result_code", "SUCCESS",
                "message", "주문 승인 완료"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "result_code", "FAIL",
                "message", "주문 승인 실패",
                "data", e.getMessage()
            ));
        }
    }



    // 주문 거절(내부직원)
    @PutMapping("/{ordCd}/reject")
    public ResponseEntity<?> rejectOrder(@PathVariable String ordCd, @RequestBody OrderVO orderVO) {
        try {
            orderVO.setOrdCd(ordCd); // path 변수 세팅
            orderVO.setOrdStatusInternal("a3"); // 내부 상태: 거절
            orderVO.setOrdStatusCustomer("s4"); // 고객 상태: 주문취소

            log.warn("거절 요청 상세 개수: {}", 
                    orderVO.getOrderDetails() != null ? orderVO.getOrderDetails().size() : "null");

            orderService.updateOrder(orderVO); // 상태와 상세까지 포함해서 업데이트

            return ResponseEntity.ok(Map.of(
                "result_code", "SUCCESS",
                "message", "주문 거절 완료"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "result_code", "FAIL",
                "message", "주문 거절 실패",
                "data", e.getMessage()
            ));
        }
    }

}
