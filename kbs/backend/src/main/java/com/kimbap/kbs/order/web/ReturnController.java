package com.kimbap.kbs.order.web;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
