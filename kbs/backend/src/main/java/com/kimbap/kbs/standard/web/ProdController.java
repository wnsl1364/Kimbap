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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kimbap.kbs.standard.service.ChangeItemVO;
import com.kimbap.kbs.standard.service.ProdService;
import com.kimbap.kbs.standard.service.ProdVO;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/std/prod")
public class ProdController {
    @Autowired
    private ProdService prodService;

    // 제품 기준정보 목록 조회
    @GetMapping("/list")
    public List<ProdVO> getProdList(){
        return prodService.getProdList();
    }

    // 제품 기준정보 등록
    @PostMapping("/insert")
    public Map<String, Object> insertProduct(@RequestBody ProdVO prod) {
        try {
            prodService.insertProd(prod);
            return Map.of(
                "success", true,
                "message", "등록 성공"
            );
        } catch (Exception e) {
            return Map.of(
                "success", false,
                "message", "등록 실패: " + e.getMessage()
            );
        }
    }

    // 제품 기준정보 단건조회
    @GetMapping("/detail/{pcode}")
    public Map<String, Object> getProductDetail(@PathVariable String pcode) {
        return prodService.getProductDetail(pcode);
    }

    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateProduct(@RequestBody ProdVO prod) {
        try {
            prodService.updateProduct(prod);

            Map<String, Object> response = Map.of(
                "success", true,
                "message", "제품 정보가 성공적으로 수정되었습니다."
            );

            return ResponseEntity.ok(response); // HTTP 200
        } catch (Exception e) {
            Map<String, Object> error = Map.of(
                "success", false,
                "message", "제품 수정 중 오류 발생: " + e.getMessage()
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error); // HTTP 500
        }
    }

    @GetMapping("/change-history/{pcode}")
    public List<ChangeItemVO> getChangeHistory(@PathVariable String pcode){
        return prodService.getChangeHistory(pcode);
    }
}
