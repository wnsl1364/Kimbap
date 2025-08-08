package com.kimbap.kbs.materials.serviceimpl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kimbap.kbs.materials.mapper.MateMapper;
import com.kimbap.kbs.materials.service.MateService;
import com.kimbap.kbs.materials.service.MaterialsVO;
import com.kimbap.kbs.materials.service.PurchaseOrderViewVO;
import com.kimbap.kbs.materials.service.SearchCriteria;

@Service
@Transactional
public class MateServiceImpl implements MateService {

    @Autowired
    private MateMapper mateMapper;

    @Override
    public void insertMateInbo(MaterialsVO mateInbo) {
        try {
            // ✅ INSERT 시에도 LOT 번호가 없으면 자동 생성 (동시성 제어 적용)
            if (mateInbo.getLotNo() == null || mateInbo.getLotNo().trim().isEmpty()) {
                String lotNumber = generateMaterialLotNumberWithLock(mateInbo.getMcode());
                mateInbo.setLotNo(lotNumber);
                System.out.println("INSERT 시 LOT 번호 자동 생성 (동시성 제어): " + lotNumber);
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
            // ✅ UPDATE 시에도 LOT 번호가 없으면 자동 생성 (입고처리 시, 동시성 제어 적용)
            if (mateInbo.getLotNo() == null || mateInbo.getLotNo().trim().isEmpty()) {
                String lotNumber = generateMaterialLotNumberWithLock(mateInbo.getMcode());
                mateInbo.setLotNo(lotNumber);
                System.out.println("UPDATE 시 LOT 번호 자동 생성 (동시성 제어): " + lotNumber);
            }

            System.out.println("=== 자재입고 수정 요청 ===");
            System.out.println("mateInboCd: " + mateInbo.getMateInboCd());
            System.out.println("fcode: " + mateInbo.getFcode());
            System.out.println("facVerCd: " + mateInbo.getFacVerCd());
            System.out.println("inboStatus: " + mateInbo.getInboStatus());
            System.out.println("lotNo: " + mateInbo.getLotNo());
            System.out.println("purcDCd: " + mateInbo.getPurcDCd());

            // 자재입고 정보 업데이트
            mateMapper.updateMateInbo(mateInbo);
            System.out.println("자재입고 수정 완료: " + mateInbo.getMateInboCd());

            if ("c5".equals(mateInbo.getInboStatus()) && mateInbo.getPurcDCd() != null) {
                System.out.println("=== 입고완료 처리 - 발주상태 업데이트 시작 ===");

                // 현재 발주 상세 정보 조회
                MaterialsVO purcOrderDetail = mateMapper.getPurcOrderDetailByCode(mateInbo.getPurcDCd());
                if (purcOrderDetail != null) {
                    Integer currentCurrQty = purcOrderDetail.getCurrQty() != null ? purcOrderDetail.getCurrQty() : 0;
                    Integer purcQty = purcOrderDetail.getPurcQty() != null ? purcOrderDetail.getPurcQty() : 0;
                    String newPurcDStatus = "";

                    if (currentCurrQty >= purcQty) {
                        newPurcDStatus = "c5"; // 입고완료 
                        System.out.println("✅ 발주 입고완료: 기존 curr_qty " + currentCurrQty + " >= purc_qty " + purcQty);
                    } else {
                        newPurcDStatus = "c3"; // 입고대기 
                        System.out.println("🔄 발주 입고대기 유지: 기존 curr_qty " + currentCurrQty + " < purc_qty " + purcQty);
                    }

                    // 발주 상세 상태만 업데이트 (CURR_QTY는 변경하지 않음)
                    MaterialsVO purcUpdateData = MaterialsVO.builder()
                            .purcDCd(mateInbo.getPurcDCd())
                            .purcDStatus(newPurcDStatus)
                            .build();

                    // 발주 상세 상태만 업데이트
                    mateMapper.updatePurcOrderDetailStatus(purcUpdateData);

                    System.out.println("✅ 발주상태만 업데이트 완료: " + mateInbo.getPurcDCd()
                            + " → 상태: " + newPurcDStatus + " (curr_qty는 변경하지 않음)");
                } else {
                    System.err.println("⚠️ 발주상세 정보를 찾을 수 없음: " + mateInbo.getPurcDCd());
                }
            }

        } catch (Exception e) {
            System.err.println("자재입고 수정 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("자재입고 수정 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 자재 LOT 번호 생성 (원자재 100, 부자재 200)
     * 
     * 📋 LOT 번호 규칙:
     * - 형식: LOT-품목유형-연월일-순번
     * - 품목유형: 100(원자재), 200(부자재)
     * - 순번: 3자리 형식, 날짜별로 1부터 시작, 자재 입고처리 시마다 증가
     * - 예시: LOT-100-20250530-001, LOT-200-20250530-002
     */
    private String generateMaterialLotNumber(String mcode) {
        try {
            // 1. 현재 날짜 (yyyyMMdd)
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            // 2. 자재 정보 조회해서 품목 유형 확인
            String mateType = getMaterialType(mcode);
            String lotTypeCode = getLotTypeByMaterialType(mateType);

            // 3. 🔍 오늘 날짜의 해당 품목유형 LOT 개수 조회 (동시성 고려)
            String lotPattern = "LOT-" + lotTypeCode + "-" + today + "-%";
            int existingCount = mateMapper.countLotsByPattern(lotPattern);

            // 4. 다음 시퀀스 = 기존 개수 + 1 (날짜별로 1부터 시작)
            int nextSequence = existingCount + 1;

            // 5. 🎯 LOT 번호 생성: LOT-품목유형-연월일-순번 (3자리 형식)
            String lotNumber = String.format("LOT-%s-%s-%03d", lotTypeCode, today, nextSequence);

            System.out.println("=== 📦 LOT 번호 생성 과정 ===");
            System.out.println("🏷️  자재코드: " + mcode);
            System.out.println("📂 품목유형: " + mateType + " → LOT코드: " + lotTypeCode);
            System.out.println("📅 오늘날짜: " + today);
            System.out.println("🔢 기존LOT수: " + existingCount + "개");
            System.out.println("⬆️  다음순번: " + nextSequence);
            System.out.println("✨ 생성결과: " + lotNumber);
            System.out.println("===============================");

            return lotNumber;

        } catch (Exception e) {
            System.err.println("❌ LOT 번호 생성 실패, 임시 번호 사용: " + e.getMessage());
            e.printStackTrace();

            // 실패 시 임시 번호 생성 (3자리 형식 유지)
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            long timestamp = System.currentTimeMillis() % 1000;
            return String.format("LOT-TMP-%s-%03d", today, timestamp);
        }
    }

    /**
     * 🔒 동시성 제어가 적용된 LOT 번호 생성 (여러 건 동시 입력 시 사용)
     * 
     * @param mcode 자재코드
     * @return 생성된 LOT 번호
     */
    @Transactional
    private String generateMaterialLotNumberWithLock(String mcode) {
        try {
            // 1. 현재 날짜 (yyyyMMdd)
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            // 2. 자재 정보 조회해서 품목 유형 확인
            String mateType = getMaterialType(mcode);
            String lotTypeCode = getLotTypeByMaterialType(mateType);

            // 3. 🔒 DB 레벨에서 동시성 제어하여 다음 시퀀스 조회
            int nextSequence = mateMapper.getNextLotSequenceWithLock(lotTypeCode, today);

            // 4. 🎯 LOT 번호 생성: LOT-품목유형-연월일-순번 (3자리 형식)
            String lotNumber = String.format("LOT-%s-%s-%03d", lotTypeCode, today, nextSequence);

            System.out.println("=== 🔒 동시성 제어 LOT 번호 생성 ===");
            System.out.println("🏷️  자재코드: " + mcode);
            System.out.println("📂 품목유형: " + mateType + " → LOT코드: " + lotTypeCode);
            System.out.println("📅 오늘날짜: " + today);
            System.out.println("🔢 다음순번: " + nextSequence + " (DB 락 적용)");
            System.out.println("✨ 생성결과: " + lotNumber);
            System.out.println("===================================");

            return lotNumber;

        } catch (Exception e) {
            System.err.println("❌ 동시성 제어 LOT 번호 생성 실패: " + e.getMessage());
            System.err.println("🔄 기본 방식으로 폴백 처리 시작...");
            e.printStackTrace();

            // 실패 시 기본 LOT 생성 방식으로 폴백
            try {
                return generateMaterialLotNumber(mcode);
            } catch (Exception fallbackException) {
                System.err.println("❌ 폴백 LOT 생성도 실패, 응급 임시 번호 생성: " + fallbackException.getMessage());
                
                // 최종 응급처리: 타임스탬프 기반 고유 번호
                String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                long timestamp = System.currentTimeMillis() % 10000; // 4자리로 제한
                return String.format("LOT-EMG-%s-%04d", today, timestamp);
            }
        }
    }

    @Override
    public MaterialsVO getPurcOrderDetailByCode(String purcDCd) {
        try {
            System.out.println("🔍 발주상세 조회: " + purcDCd);
            MaterialsVO result = mateMapper.getPurcOrderDetailByCode(purcDCd);

            if (result != null) {
                System.out.println("✅ 발주상세 조회 성공: " + purcDCd
                        + " (자재: " + result.getMcode() + " ver:" + result.getMateVerCd() + ")");
            } else {
                System.out.println("⚠️ 발주상세를 찾을 수 없음: " + purcDCd);
            }

            return result;
        } catch (Exception e) {
            System.err.println("❌ 발주상세 조회 실패: " + e.getMessage());
            throw new RuntimeException("발주상세 조회 실패: " + e.getMessage(), e);
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
     * 자재 유형을 LOT 타입 코드로 변환 h1 (원자재) → 100 h2 (부자재) → 200
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
                            System.out.println("입고코드: " + item.getMateInboCd()
                                    + " | regi: " + item.getRegi()
                                    + " | regiName: " + item.getRegiName());
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
    public List<MaterialsVO> getMateInboByPurcCd(String purcCd) {
        try {
            System.out.println("발주번호별 자재입고 데이터 조회 서비스: " + purcCd);
            List<MaterialsVO> list = mateMapper.getMateInboByPurcCd(purcCd);
            System.out.println("발주번호별 자재입고 데이터 조회 완료: " + list.size() + "건");
            return list;
        } catch (Exception e) {
            System.err.println("발주번호별 자재입고 데이터 조회 실패: " + e.getMessage());
            throw new RuntimeException("발주번호별 자재입고 데이터 조회 실패: " + e.getMessage(), e);
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
    public List<PurchaseOrderViewVO> getPurchaseOrdersForView(SearchCriteria criteria) {
        try {
            System.out.println("🎯 발주 조회 전용 서비스 시작 - 사용자 타입: " + criteria.getMemtype());

            List<PurchaseOrderViewVO> list = mateMapper.getPurchaseOrdersForView(criteria);

            System.out.println("✅ 발주 조회 완료: " + list.size() + "건");
            return list;

        } catch (Exception e) {
            System.err.println("❌ 발주 조회 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("발주 조회 실패: " + e.getMessage(), e);
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
            String lastPurcCd = mateMapper.getLastPurcCode();

            if (lastPurcCd != null && lastPurcCd.startsWith("PURC-")) {
                // "PURC-001" → "001" 추출
                String numberPart = lastPurcCd.substring(5);
                int nextNumber = Integer.parseInt(numberPart) + 1;
                // 🔥 PURC-XXX 형식! (3자리)
                System.out.println("마지막 발주번호: " + lastPurcCd + " → 다음 번호: PURC-" + String.format("%03d", nextNumber));
                return "PURC-" + String.format("%03d", nextNumber);
            } else {
                // 🔥 첫 번째는 PURC-001
                return "PURC-001";
            }
        } catch (Exception e) {
            long timestamp = System.currentTimeMillis() % 1000;
            return "PURC-" + String.format("%03d", (int) timestamp);
        }
    }

    private String generatePurcDetailCode() {
        try {
            // PURC-D-XXX 패턴으로 마지막 번호 조회
            String lastPurcDCd = mateMapper.getLastPurcDetailCode();

            if (lastPurcDCd != null && lastPurcDCd.startsWith("PURC-D-")) {
                // "PURC-D-001" → "001" 추출
                String numberPart = lastPurcDCd.substring(7); // "PURC-D-" 제거
                int nextNumber = Integer.parseInt(numberPart) + 1;
                return "PURC-D-" + String.format("%03d", nextNumber);
            } else {
                // 첫 번째는 PURC-D-001
                return "PURC-D-001";
            }
        } catch (Exception e) {
            System.err.println("발주상세번호 생성 실패, 임시번호 사용: " + e.getMessage());
            long timestamp = System.currentTimeMillis() % 1000;
            return "PURC-D-" + String.format("%03d", (int) timestamp);
        }
    }

    private String updateExistingPurchaseOrder(Map<String, Object> orderData) {
        try {
            // header 변수 선언 (빠졌던 부분!)
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

                String existingPurcDCd = (String) existingDetail.get("purcDCd");

                // 🔥 새로운 자재-거래처 조합으로 mate_cp_cd 다시 찾기!
                String mcode = (String) newDetail.get("mcode");
                String mateVerCd = (String) newDetail.getOrDefault("mateVerCd", "V1");
                String cpCd = (String) newDetail.get("cpCd");

                String mateCpCd = findMateCpCd(mcode, mateVerCd, cpCd);  // 🔥 업데이트 시에도 사용!

                MaterialsVO detailVO = MaterialsVO.builder()
                        .purcDCd(existingPurcDCd)
                        .purcCd(purcCd)
                        .mateCpCd(mateCpCd) // 🔥 새로운 mate_cp_cd!
                        .mcode(mcode)
                        .mateVerCd(mateVerCd)
                        .purcQty(convertToInteger(newDetail.get("purcQty")))
                        .unit((String) newDetail.get("unit"))
                        .unitPrice(convertToBigDecimal(newDetail.get("unitPrice")))
                        .exDeliDt(convertToDate(newDetail.get("exDeliDt")))
                        .note((String) newDetail.get("note"))
                        .purcDStatus((String) newDetail.getOrDefault("purcDStatus", "c1"))
                        .build();

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

    // 🔥 자재코드 + 거래처코드로 mate_supplier의 PK 찾기
    private String findMateCpCd(String mcode, String mateVerCd, String cpCd) {
        SearchCriteria criteria = SearchCriteria.builder()
                .mcode(mcode)
                .mateVerCd(mateVerCd)
                .cpCd(cpCd)
                .build();

        List<MaterialsVO> results = mateMapper.findMateSupplier(criteria);

        if (results.isEmpty()) {
            throw new RuntimeException("해당 자재-거래처 조합이 존재하지 않습니다: " + mcode + "-" + cpCd);
        }

        return results.get(0).getMateCpCd();
    }

    private String insertNewPurchaseOrder(Map<String, Object> orderData) {
        try {
            String purcCd = generatePurchaseCode();

            @SuppressWarnings("unchecked")
            Map<String, Object> header = (Map<String, Object>) orderData.get("header");
            header.put("purcCd", purcCd);

            // 헤더 INSERT
            MaterialsVO headerVO = MaterialsVO.builder()
                    .purcCd(purcCd)
                    .ordDt(convertToDate(header.get("ordDt")))
                    .regi((String) header.get("regi"))
                    .purcStatus((String) header.getOrDefault("purcStatus", "c1"))
                    .ordTotalAmount(convertToBigDecimal(header.get("ordTotalAmount")))
                    .build();

            mateMapper.insertPurcOrder(headerVO);

            // 🔥 상세 INSERT - 여기서 mate_cp_cd 찾아서 저장!
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> details = (List<Map<String, Object>>) orderData.get("details");

            for (Map<String, Object> detail : details) {
                String purcDCd = generatePurcDetailCode();

                // 🔥 mate_cp_cd 찾기!
                String mcode = (String) detail.get("mcode");
                String mateVerCd = (String) detail.getOrDefault("mateVerCd", "V1");
                String cpCd = (String) detail.get("cpCd");

                String mateCpCd = findMateCpCd(mcode, mateVerCd, cpCd);  // 🔥 이제 사용됨!

                MaterialsVO detailVO = MaterialsVO.builder()
                        .purcDCd(purcDCd)
                        .purcCd(purcCd)
                        .mateCpCd(mateCpCd) // 🔥 mate_supplier의 PK!
                        .mcode(mcode)
                        .mateVerCd(mateVerCd)
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
            return purcCd;

        } catch (Exception e) {
            throw new RuntimeException("새 발주서 생성 중 오류 발생: " + e.getMessage(), e);
        }
    }

    @Override
    public List<MaterialsVO> getPurcOrderDetailListForApproval() {
        return mateMapper.getPurcOrderDetailListForApproval();
    }

    @Override
    public List<MaterialsVO> getSuppliersByMaterial(SearchCriteria criteria) {
        try {
            System.out.println("=== 특정 자재의 공급업체들 조회 ===");
            System.out.println("mcode: " + criteria.getMcode());
            System.out.println("mateVerCd: " + criteria.getMateVerCd());

            List<MaterialsVO> list = mateMapper.getSuppliersByMaterial(criteria);
            System.out.println("조회 결과: " + list.size() + "건");

            return list;
        } catch (Exception e) {
            System.err.println("특정 자재의 공급업체 조회 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("특정 자재의 공급업체 조회 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public void updatePurchaseOrderStatus(MaterialsVO statusData) {
        try {
            System.out.println("🔄 발주 상태 업데이트 시작: " + statusData.getPurcDCd()
                    + " → " + statusData.getPurcDStatus());

            // 기존 발주 데이터 조회하여 검증
            List<MaterialsVO> existingData = mateMapper.getPurcOrderWithDetails(statusData.getPurcCd());
            if (existingData == null || existingData.isEmpty()) {
                throw new RuntimeException("존재하지 않는 발주코드입니다: " + statusData.getPurcCd());
            }

            // 상태 업데이트 실행
            mateMapper.updatePurcOrderDetailStatus(statusData);

            // 상태 변경이 '승인'인 경우, 발주 헤더의 상태도 업데이트
            if ("c2".equals(statusData.getPurcDStatus())) {
                MaterialsVO headerUpdate = MaterialsVO.builder()
                        .purcCd(statusData.getPurcCd())
                        .purcStatus("c2") // 헤더도 승인으로 변경
                        .build();
                mateMapper.updatePurcOrderHeaderStatus(headerUpdate);
            }

            System.out.println("✅ 발주 상태 업데이트 완료: " + statusData.getPurcDCd());

            // 알림 전송 (선택사항)
            // sendStatusChangeNotification(statusData.getPurcDCd(), statusData.getPurcDStatus(), "시스템");
        } catch (Exception e) {
            System.err.println("❌ 발주 상태 업데이트 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("발주 상태 업데이트 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public List<MaterialsVO> getPendingApprovalOrders(SearchCriteria criteria) {
        try {
            System.out.println("📋 승인 대기 발주 목록 조회 시작");

            // 승인 대기 상태(c1)로 고정
            criteria.setPurcDStatus("c1");

            List<MaterialsVO> pendingOrders = mateMapper.getPurcOrdList(criteria);

            // 승인 대기 상태만 필터링 (이중 체크)
            List<MaterialsVO> filteredOrders = pendingOrders.stream()
                    .filter(order -> "c1".equals(order.getPurcDStatus()))
                    .collect(Collectors.toList());

            System.out.println("✅ 승인 대기 발주 목록 조회 완료: " + filteredOrders.size() + "건");
            return filteredOrders;

        } catch (Exception e) {
            System.err.println("❌ 승인 대기 발주 목록 조회 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("승인 대기 발주 목록 조회 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> getPurchaseOrderStatistics(SearchCriteria criteria) {
        try {
            System.out.println("📊 발주 통계 조회 시작");

            // 전체 발주 데이터 조회
            List<MaterialsVO> allOrders = mateMapper.getPurcOrdList(criteria);

            // 상태별 집계
            Map<String, Long> statusCounts = allOrders.stream()
                    .collect(Collectors.groupingBy(
                            order -> order.getPurcDStatus() != null ? order.getPurcDStatus() : "unknown",
                            Collectors.counting()
                    ));

            // 총 금액 계산
            BigDecimal totalAmount = allOrders.stream()
                    .filter(order -> order.getUnitPrice() != null && order.getPurcQty() != null)
                    .map(order -> order.getUnitPrice().multiply(BigDecimal.valueOf(order.getPurcQty())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 월별 집계 (주문일자 기준)
            Map<String, Long> monthlyStats = allOrders.stream()
                    .filter(order -> order.getOrdDt() != null)
                    .collect(Collectors.groupingBy(
                            order -> {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
                                return sdf.format(order.getOrdDt());
                            },
                            Collectors.counting()
                    ));

            // 공급업체별 집계
            Map<String, Long> supplierStats = allOrders.stream()
                    .filter(order -> order.getCpName() != null && !order.getCpName().isEmpty())
                    .collect(Collectors.groupingBy(
                            MaterialsVO::getCpName,
                            Collectors.counting()
                    ));

            // 자재별 집계 (TOP 10)
            Map<String, Long> materialStats = allOrders.stream()
                    .filter(order -> order.getMateName() != null && !order.getMateName().isEmpty())
                    .collect(Collectors.groupingBy(
                            MaterialsVO::getMateName,
                            Collectors.counting()
                    ))
                    .entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(10)
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            LinkedHashMap::new
                    ));

            // 결과 구성
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalOrders", allOrders.size());
            statistics.put("totalAmount", totalAmount);
            statistics.put("statusCounts", statusCounts);
            statistics.put("monthlyStats", monthlyStats);
            statistics.put("supplierStats", supplierStats);
            statistics.put("topMaterials", materialStats);

            // 주요 지표들
            statistics.put("pendingApproval", statusCounts.getOrDefault("c1", 0L));
            statistics.put("approved", statusCounts.getOrDefault("c2", 0L));
            statistics.put("rejected", statusCounts.getOrDefault("c6", 0L));
            statistics.put("completed", statusCounts.getOrDefault("c5", 0L));

            System.out.println("✅ 발주 통계 조회 완료");
            return statistics;

        } catch (Exception e) {
            System.err.println("❌ 발주 통계 조회 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("발주 통계 조회 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendStatusChangeNotification(String purcDCd, String newStatus, String approver) {
        try {
            System.out.println("🔔 발주 상태 변경 알림 전송: " + purcDCd + " → " + newStatus);

            // 실제 알림 시스템 연동 시 구현
            // 예: 이메일, SMS, 시스템 알림 등
            String statusText = getStatusText(newStatus);
            String message = String.format("발주상세 %s가 %s로 변경되었습니다. (승인자: %s)",
                    purcDCd, statusText, approver);

            System.out.println("📧 알림 메시지: " + message);
            // TODO: 실제 알림 전송 로직 구현

        } catch (Exception e) {
            System.err.println("❌ 알림 전송 실패: " + e.getMessage());
            // 알림 실패는 주요 기능에 영향을 주지 않도록 예외를 던지지 않음
        }
    }

    /**
     * 상태 코드를 텍스트로 변환하는 헬퍼 메서드
     */
    private String getStatusText(String statusCode) {
        switch (statusCode) {
            case "c1":
                return "요청";
            case "c2":
                return "승인";
            case "c3":
                return "입고대기";
            case "c4":
                return "부분입고";
            case "c5":
                return "입고완료";
            case "c6":
                return "거절";
            case "c7":
                return "반품";
            default:
                return statusCode;
        }
    }

    @Override
    public List<MaterialsVO> getMaterialsBySupplier(SearchCriteria criteria) {
        try {
            System.out.println("=== 특정 거래처의 자재들 조회 ===");
            System.out.println("cpCd: " + criteria.getCpCd());

            List<MaterialsVO> list = mateMapper.getMaterialsBySupplier(criteria);
            System.out.println("조회 결과: " + list.size() + "건");

            return list;
        } catch (Exception e) {
            System.err.println("특정 거래처의 자재 조회 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("특정 거래처의 자재 조회 실패: " + e.getMessage(), e);
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

    // getSupplierMateRelList
    @Override
    public List<PurchaseOrderViewVO> getSupplierMateRelList(SearchCriteria criteria) {
        try {
            System.out.println("=== 공급업체의 출고 처리를 위한 목록 조회 시작 ===");
            System.out.println("cpCd: " + criteria.getCpCd());
            System.out.println("mcode: " + criteria.getMcode());

            List<PurchaseOrderViewVO> list = mateMapper.getSupplierMateRelList(criteria);
            System.out.println("조회 결과: " + list.size() + "건");

            return list;
        } catch (Exception e) {
            System.err.println("공급업체의 출고 처리를 위한 목록 조회 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("공급업체의 출고 처리를 위한 목록 조회 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public String getLastMateInboCode(String yearMonth) {
        try {
            System.out.println("=== ServiceImpl: 마지막 입고코드 조회 시작 ===");
            System.out.println("조회 대상 년월: " + yearMonth);
            System.out.println("검색 패턴: MATI-" + yearMonth + "-%");

            String lastCode = mateMapper.getLastMateInboCode(yearMonth);

            if (lastCode != null && !lastCode.trim().isEmpty()) {
                System.out.println("✅ 조회 성공 - 마지막 입고코드: " + lastCode);
            } else {
                System.out.println("📝 조회 결과 없음 (첫 번째 코드 생성 예정)");
            }

            return lastCode;
        } catch (Exception e) {
            System.err.println("❌ ServiceImpl: 마지막 입고코드 조회 실패");
            System.err.println("에러 메시지: " + e.getMessage());
            System.err.println("에러 타입: " + e.getClass().getSimpleName());
            e.printStackTrace();
            return null; // 조회 실패 시 null 반환
        }
    }

    @Override
    public void updatePurchaseOrderCurrQty(MaterialsVO updateData) {
        try {
            System.out.println("=== ServiceImpl: 발주상세 curr_qty 업데이트 시작 ===");
            System.out.println("발주상세코드: " + updateData.getPurcDCd());
            System.out.println("새로운 curr_qty: " + updateData.getCurrQty());

            mateMapper.updatePurchaseOrderCurrQty(updateData);

            System.out.println("✅ curr_qty 업데이트 완료");
        } catch (Exception e) {
            System.err.println("❌ ServiceImpl: curr_qty 업데이트 실패");
            System.err.println("에러 메시지: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("curr_qty 업데이트 실패: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public String generateNextMateInboCode(String yearMonth) {
        try {
            System.out.println("=== ServiceImpl: DB 원자적 입고코드 생성 시작 ===");
            System.out.println("대상 년월: " + yearMonth);

            // 🔍 현재 마지막 번호 조회 및 잠금
            String lastCode = mateMapper.getLastMateInboCodeForUpdate(yearMonth);

            int nextSequence = 1; // 기본값

            if (lastCode != null && !lastCode.trim().isEmpty()) {
                System.out.println("📋 마지막 코드: " + lastCode);
                String[] parts = lastCode.split("-");
                if (parts.length >= 3) {
                    try {
                        String numberPart = parts[2];
                        if (!numberPart.startsWith("T") && !numberPart.startsWith("E")) {
                            int lastNumber = Integer.parseInt(numberPart);
                            nextSequence = lastNumber + 1;
                            System.out.println("✅ 다음 시퀀스: " + nextSequence);
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("⚠️ 번호 파싱 실패, 기본값 사용: " + e.getMessage());
                    }
                }
            } else {
                System.out.println("📝 첫 번째 코드 생성");
            }

            String nextCode = String.format("MATI-%s-%04d", yearMonth, nextSequence);
            System.out.println("🎯 생성된 코드: " + nextCode);

            // 🔒 임시로 해당 코드 예약 (동시성 방지)
            MaterialsVO tempRecord = MaterialsVO.builder()
                    .mateInboCd(nextCode)
                    .purcDCd("TEMP-RESERVED")
                    .totalQty(0)
                    .inboStatus("RESERVED")
                    .build();

            mateMapper.insertTempMateInboReservation(tempRecord);
            System.out.println("🔒 코드 예약 완료: " + nextCode);

            return nextCode;

        } catch (Exception e) {
            System.err.println("❌ ServiceImpl: DB 원자적 코드 생성 실패");
            System.err.println("에러 메시지: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("DB 원자적 코드 생성 실패: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void updatePurchaseOrderCurrQtyAndStatus(MaterialsVO updateData) {
        try {
            System.out.println("=== ServiceImpl: curr_qty 및 상태 업데이트 시작 ===");
            System.out.println("발주상세코드: " + updateData.getPurcDCd());
            System.out.println("기존 curr_qty: " + updateData.getCurrQty());
            System.out.println("출고 수량: " + updateData.getOutboundQty());

            // 🔥 중요! 현재 curr_qty를 먼저 조회해서 누적 계산
            MaterialsVO currentData = mateMapper.getPurcOrderDetailByCode(updateData.getPurcDCd());
            if (currentData == null) {
                System.err.println("❌ 발주상세를 찾을 수 없음: " + updateData.getPurcDCd());
                throw new RuntimeException("발주상세를 찾을 수 없습니다: " + updateData.getPurcDCd());
            }

            // 현재 curr_qty 가져오기 (null이면 0으로 처리)
            Integer currentCurrQty = currentData.getCurrQty() != null ? currentData.getCurrQty() : 0;
            Integer outboundQty = updateData.getOutboundQty() != null ? updateData.getOutboundQty() : 0;

            // 🎯 누적 계산: 기존값 + 출고량
            Integer newCurrQty = currentCurrQty + outboundQty;

            System.out.println("📊 계산 상세:");
            System.out.println("  현재 curr_qty: " + currentCurrQty);
            System.out.println("  출고 수량: " + outboundQty);
            System.out.println("  새로운 curr_qty: " + newCurrQty + " (= " + currentCurrQty + " + " + outboundQty + ")");

            // 업데이트할 데이터 설정
            updateData.setCurrQty(newCurrQty);

            // 발주 수량 가져와서 상태 계산
            Integer purcQty = currentData.getPurcQty() != null ? currentData.getPurcQty() : 0;
            String newStatus = "";

            // 🔥 수정된 상태 로직 (너가 원하는 대로!)
            if (newCurrQty >= purcQty) {
                newStatus = "c3"; // ✅ 입고대기 (모든 출고 완료)
                System.out.println("✅ 상태: 입고대기 (curr_qty " + newCurrQty + " >= purc_qty " + purcQty + ")");
            } else {
                newStatus = "c2"; // ✅ 승인 (아직 출고 안끝남)
                System.out.println("🔄 상태: 승인 (curr_qty " + newCurrQty + " < purc_qty " + purcQty + ")");
            }

            updateData.setPurcDStatus(newStatus);

            // 🎯 Mapper 호출
            System.out.println("📤 Mapper.updatePurchaseOrderCurrQtyAndStatus 호출");
            mateMapper.updatePurchaseOrderCurrQtyAndStatus(updateData);

            System.out.println("✅ ServiceImpl: curr_qty 및 상태 업데이트 완료!");
            System.out.println("  최종 curr_qty: " + newCurrQty);
            System.out.println("  최종 상태: " + newStatus);

        } catch (Exception e) {
            System.err.println("❌ ServiceImpl: curr_qty 및 상태 업데이트 실패");
            System.err.println("에러 메시지: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("curr_qty 및 상태 업데이트 실패: " + e.getMessage(), e);
        }
    }

    // 자재 입출고 목록 조회
    @Override
    public List<MaterialsVO> getMaterialFlowList(MaterialsVO search) {
        return mateMapper.selectMaterialFlowList(search);
    }

    @Override
    public List<MaterialsVO> getTodayMaterialFlowList() {
        return mateMapper.selectTodayMaterialFlowList();
    }

    @Override
    public List<MaterialsVO> getMaterialStockStatus(MaterialsVO searchParams) {
        try {
            System.out.println("=== 자재 재고 현황 조회 시작 ===");
            System.out.println("검색 조건:");
            if (searchParams != null) {
                System.out.println("  - mcode: " + searchParams.getMcode());
                System.out.println("  - mateName: " + searchParams.getMateName());
                System.out.println("  - mateType: " + searchParams.getMateType());
                System.out.println("  - facName: " + searchParams.getFacName());
            }

            List<MaterialsVO> stockStatusList = mateMapper.getMaterialStockStatus(searchParams);

            // 🔧 stockPercentage 후처리 (DB에서 null인 경우 직접 계산)
            if (stockStatusList != null) {
                for (MaterialsVO item : stockStatusList) {
                    if (item.getStockPercentage() == null
                            && item.getSafeStock() != null
                            && item.getSafeStock() > 0
                            && item.getTotalQuantity() != null) {

                        double calculatedPercentage = (item.getTotalQuantity().doubleValue() / item.getSafeStock()) * 100;
                        item.setStockPercentage(Math.round(calculatedPercentage * 100.0) / 100.0); // 소수점 둘째 자리까지

                        System.out.println("🔧 stockPercentage 보정: " + item.getMaterialCode()
                                + " -> " + item.getStockPercentage() + "%");
                    }
                }
            }

            System.out.println("✅ 자재 재고 현황 조회 완료: " + stockStatusList.size() + "건");

            if (stockStatusList != null && !stockStatusList.isEmpty()) {
                // 첫 번째 데이터 로깅
                MaterialsVO firstItem = stockStatusList.get(0);
                System.out.println("🔍 첫 번째 데이터 상세:");
                System.out.println("  - materialCode: " + firstItem.getMaterialCode());
                System.out.println("  - materialName: " + firstItem.getMaterialName());
                System.out.println("  - factoryName: " + firstItem.getFactoryName());
                System.out.println("  - totalQuantity: " + firstItem.getTotalQuantity());
                System.out.println("  - safeStock: " + firstItem.getSafeStock());
                System.out.println("  - stockDifference: " + firstItem.getStockDifference());
                System.out.println("  - stockPercentage: " + firstItem.getStockPercentage());
                System.out.println("  - stockStatus: " + firstItem.getStockStatus());

                // stockPercentage 계산 검증
                if (firstItem.getSafeStock() != null && firstItem.getSafeStock() > 0 && firstItem.getTotalQuantity() != null) {
                    double calculatedPercentage = (firstItem.getTotalQuantity().doubleValue() / firstItem.getSafeStock()) * 100;
                    System.out.println("  - 계산된 stockPercentage: " + calculatedPercentage);
                    System.out.println("  - DB에서 온 stockPercentage: " + firstItem.getStockPercentage());
                }
            }

            return stockStatusList;

        } catch (Exception e) {
            System.err.println("❌ 자재 재고 현황 조회 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("자재 재고 현황 조회 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 🔍 LOT별 재고 조회
     *
     * @param mcode 자재코드
     * @return LOT별 재고 목록
     */
    @Override
    public List<MaterialsVO> getMaterialLotStock(String mcode) {
        System.out.println("🔍 LOT별 재고 조회 시작 - 자재코드: " + mcode);

        try {
            // 🚀 Mapper를 통한 LOT별 재고 조회
            List<MaterialsVO> lotStockList = mateMapper.getMaterialLotStock(mcode);

            System.out.println("✅ LOT별 재고 조회 완료 - 자재코드: " + mcode + ", 조회건수: " + lotStockList.size());

            // 🔍 디버깅용 로그
            if (!lotStockList.isEmpty()) {
                MaterialsVO firstLot = lotStockList.get(0);
                System.out.println("📊 첫 번째 LOT 정보 - LOT: " + firstLot.getLotNo()
                        + ", 수량: " + firstLot.getQuantity()
                        + ", 입고일: " + firstLot.getInboundDate());
            }

            return lotStockList;

        } catch (Exception e) {
            System.err.println("❌ LOT별 재고 조회 실패 - 자재코드: " + mcode + ", 오류: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("LOT별 재고 조회 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
}
