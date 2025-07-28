package com.kimbap.kbs.materials.serviceimpl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kimbap.kbs.materials.mapper.MateMapper;
import com.kimbap.kbs.materials.service.MateService;
import com.kimbap.kbs.materials.service.MaterialsVO;
import com.kimbap.kbs.materials.service.SearchCriteria;

@Service
@Transactional
public class MateServiceImpl implements MateService {

    @Autowired
    private MateMapper mateMapper;

    @Override
    public void insertMateInbo(MaterialsVO mateInbo) {
        try {
            // ✅ INSERT 시에도 LOT 번호가 없으면 자동 생성
            if (mateInbo.getLotNo() == null || mateInbo.getLotNo().trim().isEmpty()) {
                String lotNumber = generateMaterialLotNumber(mateInbo.getMcode());
                mateInbo.setLotNo(lotNumber);
                System.out.println("INSERT 시 LOT 번호 자동 생성: " + lotNumber);
            }
            
            mateMapper.insertMateInbo(mateInbo);  // ✅ 올바른 INSERT 호출
            System.out.println("자재입고 등록 완료: " + mateInbo.getMateInboCd());
        } catch (Exception e) {
            System.err.println("자재입고 등록 실패: " + e.getMessage());
            throw new RuntimeException("자재입고 등록 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateMateInbo(MaterialsVO mateInbo) {
        try {
            // ✅ UPDATE 시에도 LOT 번호가 없으면 자동 생성 (입고처리 시)
            if (mateInbo.getLotNo() == null || mateInbo.getLotNo().trim().isEmpty()) {
                String lotNumber = generateMaterialLotNumber(mateInbo.getMcode());
                mateInbo.setLotNo(lotNumber);
                System.out.println("UPDATE 시 LOT 번호 자동 생성: " + lotNumber);
            }

            System.out.println("=== 자재입고 수정 요청 ===");
            System.out.println("mateInboCd: " + mateInbo.getMateInboCd());
            System.out.println("fcode: " + mateInbo.getFcode());
            System.out.println("facVerCd: " + mateInbo.getFacVerCd());
            System.out.println("inboStatus: " + mateInbo.getInboStatus());
            System.out.println("lotNo: " + mateInbo.getLotNo());
            
            mateMapper.updateMateInbo(mateInbo);  // ✅ 올바른 UPDATE 호출
System.out.println("자재입고 수정 완료: " + mateInbo.getMateInboCd());
        } catch (Exception e) {
            System.err.println("자재입고 수정 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("자재입고 수정 실패: " + e.getMessage(), e);
        }
    }
      /**
     * 자재 LOT 번호 생성 (원자재 100, 부자재 200만)
     */
    private String generateMaterialLotNumber(String mcode) {
        try {
            // 1. 현재 날짜 (yyyyMMdd)
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            // 2. 자재 정보 조회해서 품목 유형 확인
            String mateType = getMaterialType(mcode);
            String lotTypeCode = getLotTypeByMaterialType(mateType);

            // 3. 오늘 날짜의 해당 품목유형 LOT 개수 조회
            String lotPattern = "LOT-" + lotTypeCode + "-" + today + "-%";
            int existingCount = mateMapper.countLotsByPattern(lotPattern);

            // 4. 다음 시퀀스 = 기존 개수 + 1 (날짜별로 1부터 시작)
            int nextSequence = existingCount + 1;

            // 5. LOT 번호 생성: LOT-품목유형-연월일-순번
            String lotNumber = String.format("LOT-%s-%s-%d", lotTypeCode, today, nextSequence);

            System.out.println("=== LOT 번호 생성 과정 ===");
            System.out.println("자재코드: " + mcode);
            System.out.println("품목유형: " + mateType + " → 코드: " + lotTypeCode);
            System.out.println("오늘날짜: " + today);
            System.out.println("기존개수: " + existingCount + "개");
            System.out.println("다음순번: " + nextSequence);
            System.out.println("생성결과: " + lotNumber);
            
            return lotNumber;

        } catch (Exception e) {
            System.err.println("LOT 생성 실패, 임시 번호 사용: " + e.getMessage());
            e.printStackTrace();
            
            // 실패 시 임시 번호 생성
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            long timestamp = System.currentTimeMillis() % 1000;
            return String.format("LOT-TMP-%s-%d", today, timestamp);
        }
    }

    /**
     * 자재 코드로 품목 유형 조회
     */
    private String getMaterialType(String mcode) {
        try {
            String mateType = mateMapper.getMaterialType(mcode);
            System.out.println("자재코드 " + mcode + "의 품목유형: " + mateType);
            return mateType != null ? mateType : "h1"; // 기본값: 원자재
        } catch (Exception e) {
            System.err.println("자재 타입 조회 실패, 기본값(h1) 사용: " + e.getMessage());
            return "h1"; // 기본값: 원자재
        }
    }

    /**
     * 자재 유형을 LOT 타입 코드로 변환
     * h1 (원자재) → 100
     * h2 (부자재) → 200
     */
    private String getLotTypeByMaterialType(String mateType) {
        if (mateType == null) {
            System.out.println("품목유형이 null, 기본값(100) 사용");
            return "100"; // 기본값: 원자재
        }

        switch (mateType.toLowerCase()) {
            case "h1":
                
                System.out.println("원자재(h1) → LOT 타입: 100");
                return "100";  // 원자재 (김, 쌀, 야채 등)
            case "h2":
                
                System.out.println("부자재(h2) → LOT 타입: 200");
                return "200";  // 부자재 (포장용지, 포장박스)
            default:
                System.out.println("알 수 없는 자재 유형 (" + mateType + "), 기본값(100) 사용");
                return "100"; // 알 수 없으면 원자재로 처리
        }
    }

    @Override
    public List<MaterialsVO> getMateInboList() {
        try {
            List<MaterialsVO> inboList = mateMapper.getMateInboList();
            System.out.println("=== 자재입고 목록 조회 결과 ===");
            
            if (inboList != null && !inboList.isEmpty()) {
                System.out.println("총 " + inboList.size() + "건 조회됨");
                
                // 첫 번째 데이터 로깅 (디버깅용)
                MaterialsVO firstItem = inboList.get(0);
                System.out.println("첫 번째 데이터:");
                System.out.println("  - purcCd: " + firstItem.getPurcCd());
                System.out.println("  - ordDt: " + firstItem.getOrdDt());
                System.out.println("  - regi: " + firstItem.getRegi());
                System.out.println("  - regiName: " + firstItem.getRegiName());  // ✅ 이게 중요!
                System.out.println("  - purcStatus: " + firstItem.getPurcStatus());
                System.out.println("  - inboStatus: " + firstItem.getInboStatus());
                System.out.println("  - mateName: " + firstItem.getMateName());
                System.out.println("  - cpName: " + firstItem.getCpName());
                
                // ✅ 모든 입고 대기 데이터의 담당자 정보도 확인
                System.out.println("=== 입고대기 상태 데이터들의 담당자 정보 ===");
                inboList.stream()
                    .filter(item -> "c3".equals(item.getInboStatus()))
                    .forEach(item -> {
                        System.out.println("입고코드: " + item.getMateInboCd() + 
                                         " | regi: " + item.getRegi() + 
                                         " | regiName: " + item.getRegiName());
                    });
                    
            } else {
                System.out.println("조회된 데이터가 없습니다.");
            }
            
            return inboList;
        } catch (Exception e) {
            System.err.println("자재입고 목록 조회 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("자재입고 목록 조회 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public MaterialsVO getMateInboById(String mateInboCd) {
        try {
            MaterialsVO mateInbo = mateMapper.getMateInboById(mateInboCd);
            System.out.println("자재입고 단건 조회 완료: " + mateInboCd);
            return mateInbo;
        } catch (Exception e) {
            System.err.println("자재입고 단건 조회 실패: " + e.getMessage());
            throw new RuntimeException("자재입고 단건 조회 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public List<MaterialsVO> getPurcOrdList() {
        return mateMapper.getPurcOrdList();
    }

    @Override
    public List<MaterialsVO> getMateRelList() {
        return mateMapper.getMateRelList();
    }

    @Override
    public void insertMateRel(MaterialsVO mateRel) {
        mateMapper.insertMateRel(mateRel);
    }

    @Override
    public List<MaterialsVO> getPurchaseOrders(SearchCriteria criteria) {
        return mateMapper.getPurcOrdList(criteria);
    }
    

    @Override
    public List<MaterialsVO> getActiveFactoryList() {
        try {
            List<MaterialsVO> factoryList = mateMapper.getActiveFactoryList();
            System.out.println("=== 공장 목록 조회 결과 ===");
            
            if (factoryList != null && !factoryList.isEmpty()) {
                System.out.println("총 " + factoryList.size() + "개 공장 조회됨");
                
                for (MaterialsVO factory : factoryList) {
                    System.out.println("  - " + factory.getFcode() + " (" + factory.getFacVerCd() + "): " + factory.getFacName());
                }
            } else {
                System.out.println("조회된 공장이 없습니다.");
            }
            
            return factoryList;
        } catch (Exception e) {
            System.err.println("공장 목록 조회 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("공장 목록 조회 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public List<MaterialsVO> getPurcOrderList() {
        return mateMapper.getPurcOrderList();
    }

    @Override
    public Map<String, Object> getPurcOrderWithDetails(String purcCd) {
        List<MaterialsVO> results = mateMapper.getPurcOrderWithDetails(purcCd);

        Map<String, Object> response = new HashMap<>();

        if (results != null && !results.isEmpty()) {
            // 첫 번째 데이터에서 발주서 헤더 정보 추출
            MaterialsVO header = results.get(0);
            Map<String, Object> headerMap = new HashMap<>();
            headerMap.put("purcCd", header.getPurcCd());
            headerMap.put("ordDt", header.getOrdDt());
            headerMap.put("regi", header.getRegi());
            headerMap.put("purcStatus", header.getPurcStatus());
            headerMap.put("ordTotalAmount", header.getOrdTotalAmount());

            response.put("header", headerMap);

            // 상세 데이터 추출 (purcDCd가 있는 것만)
            List<Map<String, Object>> details = results.stream()
                    .filter(item -> item.getPurcDCd() != null)
                    .map(item -> {
                        Map<String, Object> detail = new HashMap<>();
                        detail.put("purcDCd", item.getPurcDCd());
                        detail.put("purcCd", item.getPurcCd());
                        detail.put("cpCd", item.getCpCd());
                        detail.put("cpName", item.getCpName());
                        detail.put("mcode", item.getMcode());
                        detail.put("mateVerCd", item.getMateVerCd());
                        detail.put("mateName", item.getMateName());
                        detail.put("purcQty", item.getPurcQty());
                        detail.put("unit", item.getUnit());
                        detail.put("unitPrice", item.getUnitPrice());
                        detail.put("totalAmount", item.getPurcQty() != null && item.getUnitPrice() != null
                                ? BigDecimal.valueOf(item.getPurcQty() * item.getUnitPrice().doubleValue()) : null);
                        detail.put("exDeliDt", item.getExDeliDt());
                        detail.put("note", item.getNote());
                        detail.put("purcDStatus", item.getPurcDStatus());
                        return detail;
                    })
                    .collect(Collectors.toList());

            response.put("details", details);
        }

        return response;
    }

    @Override
    public List<MaterialsVO> getMaterialWithSuppliers(SearchCriteria criteria) {
        return mateMapper.getMaterialWithSuppliers(criteria);
    }

    @Override
    @Transactional
    public String savePurchaseOrder(Map<String, Object> orderData) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> header = (Map<String, Object>) orderData.get("header");
            String purcCd = (String) header.get("purcCd");

            // ✨ 발주번호로 기존 데이터 확인
            boolean isExisting = false;
            if (purcCd != null && !purcCd.trim().isEmpty()) {
                try {
                    List<MaterialsVO> existingData = mateMapper.getPurcOrderWithDetails(purcCd);
                    isExisting = (existingData != null && !existingData.isEmpty());
                } catch (Exception e) {
                    System.out.println("기존 데이터 확인 실패, 새로 생성으로 처리");
                    isExisting = false;
                }
            }

            if (isExisting) {
                // 🔄 UPDATE 로직
                System.out.println("📝 기존 발주서 수정: " + purcCd);
                return updateExistingPurchaseOrder(orderData);
            } else {
                // ➕ INSERT 로직 (새 발주서)
                System.out.println("✨ 새 발주서 생성");
                return insertNewPurchaseOrder(orderData);
            }

        } catch (Exception e) {
            throw new RuntimeException("발주서 저장 중 오류 발생: " + e.getMessage(), e);
        }
    }

    @Override
    public String generatePurchaseCode() {
        try {
            // ✨ DB에서 현재 최대 발주번호 조회해서 +1
            String lastPurcCd = mateMapper.getLastPurcCode();

            if (lastPurcCd != null && lastPurcCd.startsWith("PURC-")) {
                // "PURC-001" → "001" 추출
                String numberPart = lastPurcCd.substring(5);
                int nextNumber = Integer.parseInt(numberPart) + 1;
                return "PURC-" + String.format("%03d", nextNumber);
            } else {
                // 첫 번째 발주서면 PURC-001 시작
                return "PURC-001";
            }

        } catch (Exception e) {
            System.err.println("발주번호 생성 실패, 임시번호 사용: " + e.getMessage());
            // 실패시 타임스탬프 사용 (절대 중복 안됨)
            long timestamp = System.currentTimeMillis() % 10000;
            return "PURC-" + String.format("%04d", timestamp);
        }
    }

    private String updateExistingPurchaseOrder(Map<String, Object> orderData) {
        try {
            // 🔥 header 변수 선언 (빠졌던 부분!)
            @SuppressWarnings("unchecked")
            Map<String, Object> header = (Map<String, Object>) orderData.get("header");
            String purcCd = (String) header.get("purcCd");

            // 1️⃣ 헤더 UPDATE (headerVO 변수 선언!)
            MaterialsVO headerVO = MaterialsVO.builder()
                    .purcCd(purcCd)
                    .ordDt(convertToDate(header.get("ordDt")))
                    .regi((String) header.get("regi"))
                    .purcStatus((String) header.getOrDefault("purcStatus", "c1"))
                    .ordTotalAmount(convertToBigDecimal(header.get("ordTotalAmount")))
                    .build();

            mateMapper.updatePurcOrder(headerVO);
            System.out.println("✅ 발주 헤더 수정 완료");

            // 2️⃣ 🔥 기존 발주상세 데이터 가져오기
            Map<String, Object> existingOrder = getPurcOrderWithDetails(purcCd);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> existingDetails = (List<Map<String, Object>>) existingOrder.get("details");

            System.out.println("🔍 기존 발주상세들:");
            for (Map<String, Object> detail : existingDetails) {
                System.out.println("  - " + detail.get("purcDCd"));
            }

            // 3️⃣ 🔥 새 데이터와 기존 데이터를 매칭해서 UPDATE
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> newDetails = (List<Map<String, Object>>) orderData.get("details");

            for (int i = 0; i < newDetails.size() && i < existingDetails.size(); i++) {
                Map<String, Object> newDetail = newDetails.get(i);
                Map<String, Object> existingDetail = existingDetails.get(i);

                // 🔥 기존 데이터의 purc_d_cd 그대로 사용!
                String existingPurcDCd = (String) existingDetail.get("purcDCd");
                String existingMateVerCd = (String) existingDetail.get("mateVerCd");  // V001
                String existingMcode = (String) existingDetail.get("mcode");
                String existingCpCd = (String) existingDetail.get("cpCd");

                System.out.println("🔄 기존 코드로 업데이트: " + existingPurcDCd);

                MaterialsVO detailVO = MaterialsVO.builder()
                        .purcDCd(existingPurcDCd) // 🔥 기존 코드
                        .purcCd(purcCd)
                        .cpCd(existingCpCd) // 🔥 기존 거래처코드
                        .mcode(existingMcode) // 🔥 기존 자재코드  
                        .mateVerCd(existingMateVerCd) // 🔥 기존 버전코드 (V001)
                        .purcQty(convertToInteger(newDetail.get("purcQty"))) // 🔥 수량만 새 값!
                        .unit((String) newDetail.get("unit"))
                        .unitPrice(convertToBigDecimal(newDetail.get("unitPrice")))
                        .exDeliDt(convertToDate(newDetail.get("exDeliDt")))
                        .note((String) newDetail.get("note"))
                        .purcDStatus((String) newDetail.getOrDefault("purcDStatus", "c1"))
                        .build();

                // 🔥 void 메서드이므로 int로 받지 말고 그냥 호출만!
                mateMapper.updatePurcOrderDetail(detailVO);
                System.out.println("✅ 발주상세 업데이트 완료: " + existingPurcDCd);
            }

            System.out.println("✅ 전체 발주서 수정 완료: " + purcCd);
            return purcCd;

        } catch (Exception e) {
            System.err.println("❌ 발주서 수정 실패: " + e.getMessage());
            throw new RuntimeException("발주서 수정 중 오류 발생: " + e.getMessage(), e);
        }
    }

    private String insertNewPurchaseOrder(Map<String, Object> orderData) {
        try {
            // 🔥 발주번호 자동생성 (여기서!)
            String purcCd = generatePurchaseCode();

            @SuppressWarnings("unchecked")
            Map<String, Object> header = (Map<String, Object>) orderData.get("header");

            // 🔥 프론트에서 빈값 보내도 여기서 자동생성된 번호로 덮어쓰기!
            header.put("purcCd", purcCd);

            System.out.println("✨ 자동생성된 발주번호: " + purcCd);

            // 헤더 INSERT
            MaterialsVO headerVO = MaterialsVO.builder()
                    .purcCd(purcCd) // 🔥 자동생성된 번호 사용!
                    .ordDt(convertToDate(header.get("ordDt")))
                    .regi((String) header.get("regi"))
                    .purcStatus((String) header.getOrDefault("purcStatus", "c1"))
                    .ordTotalAmount(convertToBigDecimal(header.get("ordTotalAmount")))
                    .build();

            mateMapper.insertPurcOrder(headerVO);

            // 상세 INSERT
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> details = (List<Map<String, Object>>) orderData.get("details");
            for (int i = 0; i < details.size(); i++) {
                Map<String, Object> detail = details.get(i);

                // 🔥 발주상세코드도 자동생성! (PURC-003-D001, PURC-003-D002...)
                String purcDCd = purcCd + "-D" + String.format("%03d", i + 1);

                MaterialsVO detailVO = MaterialsVO.builder()
                        .purcDCd(purcDCd) // 🔥 자동생성!
                        .purcCd(purcCd) // 🔥 자동생성!
                        .cpCd((String) detail.get("cpCd"))
                        .mcode((String) detail.get("mcode"))
                        .mateVerCd((String) detail.getOrDefault("mateVerCd", "V1"))
                        .purcQty(convertToInteger(detail.get("purcQty")))
                        .unit((String) detail.get("unit"))
                        .unitPrice(convertToBigDecimal(detail.get("unitPrice")))
                        .exDeliDt(convertToDate(detail.get("exDeliDt")))
                        .note((String) detail.get("note"))
                        .purcDStatus((String) detail.getOrDefault("purcDStatus", "c1"))
                        .build();

                mateMapper.insertPurcOrderDetail(detailVO);
            }

            System.out.println("✅ 새 발주서 생성 완료: " + purcCd);
            return purcCd;  // 🔥 자동생성된 번호 리턴!

        } catch (Exception e) {
            throw new RuntimeException("새 발주서 생성 중 오류 발생: " + e.getMessage(), e);
        }
    }

    private Date convertToDate(Object dateObj) {
        if (dateObj == null) {
            return null;
        }

        if (dateObj instanceof Date) {
            return (Date) dateObj;
        }

        if (dateObj instanceof String) {
            try {
                String dateStr = ((String) dateObj).trim();
                if (dateStr.isEmpty()) {
                    return null;
                }

                if (dateStr.contains("T")) {
                    return Date.from(Instant.parse(dateStr));
                } else {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    return sdf.parse(dateStr);
                }
            } catch (Exception e) {
                System.err.println("날짜 변환 실패: " + dateObj + " - " + e.getMessage());
                return null;
            }
        }

        return null;
    }

    private Integer convertToInteger(Object obj) {
        if (obj == null) {
            return 0;
        }

        if (obj instanceof Integer) {
            return (Integer) obj;
        }

        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }

        if (obj instanceof String) {
            try {
                String str = ((String) obj).trim();
                if (str.isEmpty()) {
                    return 0;
                }

                // 소수점이 있는 경우 처리 (9.0 → 9)
                if (str.contains(".")) {
                    return (int) Double.parseDouble(str);
                }

                return Integer.parseInt(str);
            } catch (NumberFormatException e) {
                System.err.println("Integer 변환 실패: " + obj + " → 기본값 0 사용");
                return 0;
            }
        }

        // 예상치 못한 타입
        System.err.println("예상치 못한 타입: " + obj.getClass().getSimpleName() + " → 기본값 0 사용");
        return 0;
    }

    private BigDecimal convertToBigDecimal(Object obj) {
        if (obj == null) {
            return BigDecimal.ZERO;
        }

        if (obj instanceof BigDecimal) {
            return (BigDecimal) obj;
        }

        if (obj instanceof Number) {
            return BigDecimal.valueOf(((Number) obj).doubleValue());
        }

        if (obj instanceof String) {
            try {
                String str = ((String) obj).trim();
                if (str.isEmpty()) {
                    return BigDecimal.ZERO;
                }
                return new BigDecimal(str);
            } catch (NumberFormatException e) {
                System.err.println("BigDecimal 변환 실패: " + obj + " → 기본값 0 사용");
                return BigDecimal.ZERO;
            }
        }

        // 예상치 못한 타입
        System.err.println("예상치 못한 타입: " + obj.getClass().getSimpleName() + " → 기본값 0 사용");
        return BigDecimal.ZERO;
    }
}
