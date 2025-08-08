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


// 자재 적재 대기 목록 전체 조회

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


// 특정 입고번호의 적재 대기 자재 단건 조회

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


// 단건 자재 적재 처리

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

// 다중 자재 적재 처리 (선택된 여러 자재 한번에 처리)

    @PostMapping("/processBatch")
    public ResponseEntity<Map<String, Object>> processMateLoadingBatch(@RequestBody List<MateLoadingVO> mateLoadingList) {
        try {
            System.out.println("=== 다중 자재 적재 처리 요청 ===");
            System.out.println("요청 건수: " + mateLoadingList.size());
            
            // 각 항목의 상세 정보 로그
            for (int i = 0; i < mateLoadingList.size(); i++) {
                MateLoadingVO item = mateLoadingList.get(i);
                System.out.println(String.format("[%d] mateInboCd: %s, mcode: %s, qty: %s, wareAreaCd: %s, unit: %s", 
                    i+1, item.getMateInboCd(), item.getMcode(), item.getQty(), item.getWareAreaCd(), item.getUnit()));
            }
            
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


// 활성화된 공장 목록 조회 (검색조건 드롭다운용)

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


// 특정 공장의 창고 목록 조회 (창고 유형별)

    @GetMapping("/warehouses")
    public ResponseEntity<List<MateLoadingVO>> getWarehousesByFactory(@RequestParam String fcode) {
        try {
            System.out.println("공장별 창고 목록 조회 요청: " + fcode);
            
            List<MateLoadingVO> warehouseList = mateLoadingService.getWarehousesByFactory(fcode);
            
            System.out.println("공장별 창고 목록 조회 완료: " + fcode + " - " + warehouseList.size() + "개");
            return ResponseEntity.ok(warehouseList);
            
        } catch (Exception e) {
            System.err.println("공장별 창고 목록 조회 실패: " + fcode + " - " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    

// 특정 창고의 구역 정보 조회 (층별, 현재 적재 상황 포함)

    @GetMapping("/warehouse-areas")
    public ResponseEntity<List<Map<String, Object>>> getWarehouseAreasWithStock(
            @RequestParam String wcode, 
            @RequestParam Integer floor) {
        try {
            System.out.println("창고 구역별 적재 현황 조회 요청: " + wcode + " " + floor + "층");
            
            List<Map<String, Object>> areaList = mateLoadingService.getWarehouseAreasWithStock(wcode, floor);
            
            System.out.println("창고 구역별 적재 현황 조회 완료: " + wcode + " " + floor + "층 - " + areaList.size() + "개 구역");
            return ResponseEntity.ok(areaList);
            
        } catch (Exception e) {
            System.err.println("창고 구역별 적재 현황 조회 실패: " + wcode + " " + floor + "층 - " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    

// 창고구역코드 조회

    @GetMapping("/warehouse-area-code")
    public ResponseEntity<Map<String, String>> getWareAreaCode(
            @RequestParam String wcode,
            @RequestParam String areaRow,
            @RequestParam Integer areaCol,
            @RequestParam Integer areaFloor) {
        try {
            System.out.println("창고구역코드 조회 요청: " + wcode + "-" + areaRow + areaCol + "-" + areaFloor);
            
            String wareAreaCode = mateLoadingService.getWareAreaCode(wcode, areaRow, areaCol, areaFloor);
            
            Map<String, String> response = new HashMap<>();
            response.put("wareAreaCd", wareAreaCode);
            response.put("wcode", wcode);
            response.put("areaRow", areaRow);
            response.put("areaCol", areaCol.toString());
            response.put("areaFloor", areaFloor.toString());
            
            System.out.println("창고구역코드 조회 완료: " + wareAreaCode);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("창고구역코드 조회 실패: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    

// 구역 적재 가능 여부 검증

    @GetMapping("/validate-area")
    public ResponseEntity<Map<String, Object>> validateAreaAllocation(
            @RequestParam String wareAreaCd,
            @RequestParam String mcode,
            @RequestParam Integer allocateQty) {
        try {
            System.out.println("구역 적재 검증 요청: " + wareAreaCd + " - " + mcode + " - " + allocateQty);
            
            Map<String, Object> validationResult = mateLoadingService.validateAreaAllocation(wareAreaCd, mcode, allocateQty);
            
            System.out.println("구역 적재 검증 완료: " + validationResult.get("message"));
            return ResponseEntity.ok(validationResult);
            
        } catch (Exception e) {
            System.err.println("구역 적재 검증 실패: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("isValid", false);
            errorResponse.put("message", "구역 적재 검증 중 오류가 발생했습니다: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    

// 동일한 자재가 적재된 다른 구역들 조회 (분할 적재용)

    @GetMapping("/same-material-areas")
    public ResponseEntity<List<MateLoadingVO>> getSameMaterialAreas(
            @RequestParam String mcode,
            @RequestParam String fcode,
            @RequestParam(required = false) String excludeAreaCd) {
        try {
            System.out.println("동일 자재 적재 구역 조회 요청: " + mcode + " - " + fcode);
            
            if (excludeAreaCd == null) excludeAreaCd = "";
            
            List<MateLoadingVO> sameMaterialAreas = mateLoadingService.getSameMaterialAreas(mcode, fcode, excludeAreaCd);
            
            System.out.println("동일 자재 적재 구역 조회 완료: " + mcode + " - " + sameMaterialAreas.size() + "개 구역");
            return ResponseEntity.ok(sameMaterialAreas);
            
        } catch (Exception e) {
            System.err.println("동일 자재 적재 구역 조회 실패: " + mcode + " - " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }


//  창고재고목록코드 생성 (새로운 순번 생성)
    @GetMapping("/wslcode")
    public ResponseEntity<Map<String, String>> getWslCodeByArea(@RequestParam String wareAreaCd) {
        try {
            // 새로운 순번으로 창고재고목록코드 생성
            String wslCode = mateLoadingService.generateWareStockCode();
            
            Map<String, String> response = new HashMap<>();
            response.put("wareAreaCd", wareAreaCd);
            response.put("wslCode", wslCode);
            
            System.out.println("새로운 wslcode 생성 완료: " + wareAreaCd + " -> " + wslCode);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("wslcode 생성 실패: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}