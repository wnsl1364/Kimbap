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

import com.kimbap.kbs.order.service.ReturnItemVO;
import com.kimbap.kbs.order.service.ReturnRequestVO;
import com.kimbap.kbs.order.service.ReturnService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/return")
@RequiredArgsConstructor
public class ReturnController {

    private final ReturnService returnService;

    // 반품 등록
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerReturn(@RequestBody ReturnRequestVO request) {
        Map<String, Object> response = new HashMap<>();
        try {
            returnService.registerReturn(request);

            response.put("result_code", "SUCCESS");
            response.put("message", "반품 요청 등록 성공");
            response.put("data", request);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("반품 등록 중 오류 발생", e);
            response.put("result_code", "FAIL");
            response.put("message", "반품 요청 실패");
            response.put("data", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 반품이력조회
    @GetMapping("/history/{ordCd}")
    public ResponseEntity<Map<String, Object>> getReturnHistory(@PathVariable String ordCd) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<ReturnItemVO> history = returnService.getReturnHistoryByOrdCd(ordCd);
            response.put("result_code", "SUCCESS");
            response.put("message", "반품 이력 조회 성공");
            response.put("data", history);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("반품 이력 조회 중 오류 발생", e);
            response.put("result_code", "FAIL");
            response.put("message", "반품 이력 조회 실패");
            response.put("data", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 반품 목록 조회
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getReturnList(@RequestParam Map<String, String> paramMap) {
        Map<String, Object> params = new HashMap<>();

        params.put("cpName", paramMap.getOrDefault("cpName", ""));
        params.put("prodName", paramMap.getOrDefault("prodName", ""));
        params.put("returnStatusInternal", paramMap.getOrDefault("returnStatusInternal", ""));
        params.put("startDate", paramMap.getOrDefault("startDate", ""));
        params.put("endDate", paramMap.getOrDefault("endDate", ""));

        log.info("검색 파라미터: {}", params);

        Map<String, Object> response = new HashMap<>();
        try {
            List<ReturnItemVO> returnList = returnService.getReturnList(params);
            response.put("result_code", "SUCCESS");
            response.put("message", "반품 목록 조회 성공");
            response.put("data", returnList);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("반품 목록 조회 실패", e);
            response.put("result_code", "FAIL");
            response.put("message", "반품 목록 조회 실패");
            response.put("data", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    // 반품 승인 처리 (다중)
    @PutMapping("/approve")
    public ResponseEntity<Map<String, Object>> approveReturn(@RequestBody List<ReturnItemVO> requests) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (requests == null || requests.isEmpty()) {
                response.put("result_code", "FAIL");
                response.put("message", "승인할 반품건이 없습니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // 여러 건 처리
            returnService.approveReturns(requests);

            response.put("result_code", "SUCCESS");
            response.put("message", "반품 승인 성공 (" + requests.size() + "건)");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("반품 승인 실패", e);
            response.put("result_code", "FAIL");
            response.put("message", "반품 승인 실패");
            response.put("data", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 반품 거절 처리
    @PutMapping("/reject")
    public ResponseEntity<Map<String, Object>> rejectReturn(@RequestBody ReturnItemVO request) {
        Map<String, Object> response = new HashMap<>();
        try {
            returnService.rejectReturn(request);
            response.put("result_code", "SUCCESS");
            response.put("message", "반품 거절 성공");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("반품 거절 실패", e);
            response.put("result_code", "FAIL");
            response.put("message", "반품 거절 실패");
            response.put("data", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 반품 취소 (주문상세 단위)
    @PutMapping("/cancel")
    public ResponseEntity<Map<String, Object>> cancelReturn(@RequestBody List<String> ordDCdList) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (ordDCdList == null || ordDCdList.isEmpty()) {
                response.put("result_code", "FAIL");
                response.put("message", "취소할 주문상세가 없습니다.");
                return ResponseEntity.badRequest().body(response);
            }

            returnService.cancelReturnItems(ordDCdList);

            response.put("result_code", "SUCCESS");
            response.put("message", "반품 취소 성공");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("반품 취소 실패", e);
            response.put("result_code", "FAIL");
            response.put("message", "반품 취소 중 오류 발생");
            response.put("data", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // LOT 목록 조회
    @GetMapping("/lot/{ordDCd}")
    public ResponseEntity<?> getLotList(@PathVariable String ordDCd) {
        List<String> lotList = returnService.getLotList(ordDCd);
        return ResponseEntity.ok().body(Map.of("result_code", "SUCCESS", "data", lotList));
    }
}
