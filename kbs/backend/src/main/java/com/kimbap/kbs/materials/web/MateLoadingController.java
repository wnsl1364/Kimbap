package com.kimbap.kbs.materials.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kimbap.kbs.materials.service.MateLoadingService;
import com.kimbap.kbs.materials.service.MateLoadingVO;

@RestController
@RequestMapping("/api/materials/mateLoading")
@CrossOrigin(origins = "*")
public class MateLoadingController {

    @Autowired
    private MateLoadingService mateLoadingService;

    /**
     * 자재 적재 대기 목록 전체 조회
     * @return 적재 대기 자재 목록
     */
    @GetMapping("/waitList")
    public ResponseEntity<List<MateLoadingVO>> getAllMateLoadingWaitList() {
        try {
            List<MateLoadingVO> list = mateLoadingService.getAllMateLoadingWaitList();
            System.out.println("자재 적재 대기 목록 조회 완료: " + list.size() + "건");
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            System.err.println("자재 적재 대기 목록 조회 실패: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 특정 입고번호의 적재 대기 자재 단건 조회
     * @param mateInboCd 자재입고코드
     * @return 적재 대기 자재 정보
     */
    @GetMapping("/detail/{mateInboCd}")
    public ResponseEntity<MateLoadingVO> getMateLoadingByInboCd(@PathVariable String mateInboCd) {
        try {
            MateLoadingVO result = mateLoadingService.getMateLoadingByInboCd(mateInboCd);
            if (result != null) {
                System.out.println("자재 적재 대기 단건 조회 완료: " + mateInboCd);
                return ResponseEntity.ok(result);
            } else {
                System.out.println("해당 입고번호를 찾을 수 없음: " + mateInboCd);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("자재 적재 대기 단건 조회 실패: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 단건 자재 적재 처리
     * @param mateLoading 적재할 자재 정보
     * @return 처리 결과 메시지
     */
    @PostMapping("/processSingle")
    public ResponseEntity<Map<String, Object>> processMateLoading(@RequestBody MateLoadingVO mateLoading) {
        try {
            System.out.println("단건 자재 적재 처리 요청: " + mateLoading.getMateInboCd());
            
            String result = mateLoadingService.processMateLoading(mateLoading);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", result);
            response.put("mateInboCd", mateLoading.getMateInboCd());
            
            System.out.println("단건 자재 적재 처리 완료: " + mateLoading.getMateInboCd());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("단건 자재 적재 처리 실패: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "자재 적재 처리 중 오류가 발생했습니다: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 다중 자재 적재 처리 (선택된 여러 자재 한번에 처리)
     * @param mateLoadingList 적재할 자재 목록
     * @return 처리 결과 메시지
     */
    @PostMapping("/processBatch")
    public ResponseEntity<Map<String, Object>> processMateLoadingBatch(@RequestBody List<MateLoadingVO> mateLoadingList) {
        try {
            System.out.println("다중 자재 적재 처리 요청: " + mateLoadingList.size() + "건");
            
            if (mateLoadingList.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "처리할 자재를 선택해주세요.");
                
                return ResponseEntity.badRequest().body(response);
            }
            
            String result = mateLoadingService.processMateLoadingBatch(mateLoadingList);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", result);
            response.put("processedCount", mateLoadingList.size());
            
            System.out.println("다중 자재 적재 처리 완료: " + mateLoadingList.size() + "건");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("다중 자재 적재 처리 실패: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "다중 자재 적재 처리 중 오류가 발생했습니다: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 활성화된 공장 목록 조회 (검색조건 드롭다운용)
     * @return 활성화된 공장 목록
     */
    @GetMapping("/factories")
    public ResponseEntity<List<MateLoadingVO>> getActiveFactoryList() {
        try {
            List<MateLoadingVO> factoryList = mateLoadingService.getActiveFactoryList();
            System.out.println("공장 목록 조회 완료: " + factoryList.size() + "개");
            return ResponseEntity.ok(factoryList);
        } catch (Exception e) {
            System.err.println("공장 목록 조회 실패: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 창고 구역별 wslcode 조회 (위치선택 시 사용)
     * @param wareAreaCd 창고구역코드
     * @return wslcode
     */
    @GetMapping("/wslcode")
    public ResponseEntity<Map<String, String>> getWslCodeByArea(@RequestParam String wareAreaCd) {
        try {
            String wslCode = mateLoadingService.getWslCodeByArea(wareAreaCd);
            
            Map<String, String> response = new HashMap<>();
            response.put("wareAreaCd", wareAreaCd);
            response.put("wslCode", wslCode);
            
            System.out.println("wslcode 조회 완료: " + wareAreaCd + " -> " + wslCode);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("wslcode 조회 실패: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}