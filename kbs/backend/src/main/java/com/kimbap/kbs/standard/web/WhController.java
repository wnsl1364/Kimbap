package com.kimbap.kbs.standard.web;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.kimbap.kbs.standard.service.WhService;
import com.kimbap.kbs.standard.service.WhVO;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/std/wh")
public class WhController {
    
    private static final Logger log = LoggerFactory.getLogger(WhController.class);
    
    @Autowired
    private WhService whService;

    // 창고 목록 조회
    @GetMapping("/list")
    public List<WhVO> getWarehouseList(){
        return whService.getWarehouseList();
    }

    // 창고 기준정보 등록
    @PostMapping("/insert")
    public Map<String, Object> insertWh(@RequestBody WhVO wh){
        try {
            whService.insertWh(wh);
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

    //
    // 창고 기준정보 단건조회
    @GetMapping("/detail/{wcode}")
    public Map<String,Object> getWhdetail(@PathVariable String wcode){
        return whService.getWhdetail(wcode);
    }

    // 창고 기준정보 수정
    @PutMapping("/update")
    public ResponseEntity<Map<String,Object>> updateWh(@RequestBody WhVO wh)  {
        try {
            whService.updateWh(wh);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "창고 정보가 성공적으로 수정되었습니다."
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("전체 에러:", e); // 👈 이 줄 추가
            Map<String, Object> error = Map.of(
                "success", false,
                "message", "창고 수정 중 오류 발생: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 변경 이력조회
    @GetMapping("/change-history/{wcode}")
    public List<ChangeItemVO> getChangeHistory(@PathVariable String wcode){
        return whService.getChangeHistory(wcode);
    }
}
