package com.kimbap.kbs.standard.web;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    public ResponseEntity<Map<String, Object>> registerMat(@RequestBody MatVO mat) {
        try {
            matService.insertMatWithSuppliers(mat);

            Map<String, Object> response = Map.of(
                "success", true,
                "message", "등록 성공"
            );
            return ResponseEntity.ok(response); // ✅ 헤더 생략
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = Map.of(
                "success", false,
                "message", "등록 실패"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // ✅ 헤더 생략
        }
    }
    @GetMapping("/detail/{mcode}")
    public Map<String, Object> getMaterialDetail(@PathVariable String mcode) {
        return matService.getMaterialDetail(mcode);
    }
}