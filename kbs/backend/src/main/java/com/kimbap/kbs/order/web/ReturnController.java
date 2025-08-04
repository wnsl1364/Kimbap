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
    public ResponseEntity<Map<String, Object>> getReturnList() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<ReturnItemVO> returnList = returnService.getReturnList(new HashMap<>()); // 파라미터 없으면 전체 조회
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

    // 반품 승인 처리
    @PutMapping("/approve")
    public ResponseEntity<Map<String, Object>> approveReturn(@RequestBody List<String> prodReturnCds) {
        Map<String, Object> response = new HashMap<>();
        try {
            returnService.approveReturn(prodReturnCds);
            response.put("result_code", "SUCCESS");
            response.put("message", "반품 승인 성공");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("반품 승인 실패", e);
            response.put("result_code", "FAIL");
            response.put("message", "반품 승인 실패");
            response.put("data", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 반품 반려 처리
    @PutMapping("/reject")
    public ResponseEntity<Map<String, Object>> rejectReturn(@RequestBody List<String> prodReturnCds) {
        Map<String, Object> response = new HashMap<>();
        try {
            returnService.rejectReturn(prodReturnCds);
            response.put("result_code", "SUCCESS");
            response.put("message", "반품 반려 성공");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("반품 반려 실패", e);
            response.put("result_code", "FAIL");
            response.put("message", "반품 반려 실패");
            response.put("data", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
