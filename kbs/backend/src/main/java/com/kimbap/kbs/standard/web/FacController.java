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
import com.kimbap.kbs.standard.service.FacService;
import com.kimbap.kbs.standard.service.FacVO;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/std/fac")
public class FacController {

    @Autowired
    private FacService facService;

    // 공장 기준정보 목록 조회
    @GetMapping("/list")
    public List<FacVO> getFacList(){
        return facService.getFacList();
    }

    // 공장 기준정보 등록
    @PostMapping("/insert")
    public ResponseEntity<Map<String,Object>> insertFac(@RequestBody FacVO fac) {
        try {
            facService.insertFacWithMax(fac);

            Map<String,Object> response = Map.of(
                "success", true,
                "message", "등록 성공"
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String,Object> response = Map.of(
                "success", false,
                "message", "등록 실패"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 공장 기준정보 단건 조회
    @GetMapping("/detail/{fcode}")
    public Map<String,Object> getFactoryDetail(@PathVariable String fcode) {
        return facService.getFacDetail(fcode);
    }

    // 공장 기준정보 수정
    @PutMapping("/update")
    public ResponseEntity<Map<String,Object>> updateFac(@RequestBody FacVO fac){
        try {
            facService.updateFactory(fac);
            Map<String,Object> response = Map.of(
                "success", true,
                "message", "수정 성공"
            );
            return ResponseEntity.ok(response);
        }catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = Map.of(
                "success", false,
                "message", "수정 실패: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }   
    @GetMapping("/change-history/{fcode}")
    public List<ChangeItemVO> getChangeHistory (@PathVariable String fcode) {
        return facService.getChangeHistory(fcode);
    }
}
