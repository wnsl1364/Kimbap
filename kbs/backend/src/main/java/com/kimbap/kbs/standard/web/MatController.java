package com.kimbap.kbs.standard.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kimbap.kbs.standard.service.MatService;
import com.kimbap.kbs.standard.service.MatVO;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/std/mat")
public class MatController {

    @Autowired
    private MatService matService;

    // 자재 목록 조회
    @GetMapping("/list")
    public List<MatVO> getMatList() {
        return matService.getMatList();
    }

    @PostMapping("/insert")
    public ResponseEntity<String> registerMat(@RequestBody MatVO mat) {
        try {
            matService.insertMatWithSuppliers(mat); // 서비스 로직 실행
             return ResponseEntity
            .ok()
            .header("Content-Type", "application/json; charset=UTF-8")
            .body("등록 성공");
        } catch (Exception e) {
            e.printStackTrace(); // 서버 로그 확인용
            return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .header("Content-Type", "application/json; charset=UTF-8")
            .body("등록 실패");
        }
    }
}