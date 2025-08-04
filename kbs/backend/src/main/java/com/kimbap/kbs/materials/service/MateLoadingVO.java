package com.kimbap.kbs.materials.service;

import java.math.BigDecimal;
import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MateLoadingVO {
    
    // ========== 기존 공통 컬럼들 ==========
    private String mcode;           // 자재 코드
    private String mateVerCd;       // 자재버전코드
    private String wcode;           // 창고코드
    private String wareVerCd;       // 창고버전코드
    private String fcode;           // 공장코드
    private String facVerCd;        // 공장버전코드
    private String wareAreaCd;      // 창고구역코드
    private String unit;            // 단위
    private String note;            // 비고
    private String isUsed;          // 사용여부
    private String regi;            // 등록자
    private Timestamp inboDt;       // 입고일자
    private String startDate;       // 입고일자 시작일
    private String endDate;         // 입고일자 종료일
    
    // ========== 기존 mate_inbo 테이블 컬럼들 ==========
    private String mateInboCd;      // 자재입고코드
    private String purcDCd;         // 구매상세코드
    private String mateInboDCd;     // 자재입고상세코드
    private String lotNo;           // 로트번호
    private String supplierLotNo;   // 공급업체로트번호
    private String inboStatus;      // 입고상태
    private BigDecimal totalQty;    // 총수량
    private String mname;           // 담당자명
    private String cpCd;            // 회사코드
    private Timestamp deliDt;       // 배송일자
    
    // ========== 기존 material 테이블 컬럼들 ==========
    private String mateName;        // 자재명
    private String mateType;        // 자재타입
    private String stoCon;          // 보관조건
    private String std;             // 규격
    private String pieceUnit;       // 개당단위
    private BigDecimal converQty;   // 환산수량
    private BigDecimal moqty;       // 최소주문수량
    private BigDecimal safeStock;   // 안전재고
    private BigDecimal edate;       // 유효기간
    private String corigin;         // 원산지
    private String modi;            // 수정자
    private String chaRea;          // 변경사유
    private Timestamp regDt;        // 등록일시
    
    // ========== 기존 ware_stock 테이블 컬럼들 ==========
    private String wslcode;         // 창고재고위치코드
    private String prodInboCd;      // 제품입고코드
    private String itemType;        // 품목유형
    private BigDecimal qty;         // 수량

    // ========== 기존 ware_d 테이블 컬럼들 ==========
    private String areaRow;         // 구역행 (A, B, C...)
    private BigDecimal areaCol;     // 구역열 (1, 2, 3...)
    private BigDecimal areaFloor;   // 구역층 (1, 2, 3...)
    private BigDecimal vol;         // 용량
    
    // ========== 기존 warehouse 테이블 컬럼들 ==========
    private String wareName;        // 창고명
    private String wareType;        // 창고타입
    private String address;         // 주소
    private BigDecimal maxRow;      // 최대행
    private BigDecimal maxCol;      // 최대열
    private BigDecimal maxFloor;    // 최대층
    
    // ========== 기존 factory 테이블 컬럼들 ==========
    private String facName;         // 공장명
    private String tel;             // 연락처
    private String opStatus;        // 가동상태

    // ========== 창고 구역 선택 관련 신규 필드들 ==========
    
    /**
     * 현재 적재량 (계산 필드)
     * 해당 구역에 현재 적재된 총 수량
     */
    private Integer currentVolume;
    
    /**
     * 잔여 용량 (계산 필드)
     * vol - currentVolume으로 계산되는 값
     */
    private Integer availableVolume;
    
    /**
     * 현재 적재된 자재코드 (조회 필드)
     * 해당 구역에 적재된 자재의 mcode (동일 자재 체크용)
     */
    private String currentMaterial;
    
    /**
     * 현재 적재된 자재명 (조회 필드)
     * 현재 적재된 자재의 이름
     */
    private String currentMaterialName;
    
    /**
     * 적재 가능 여부 (계산 필드)
     * 동일 자재이고 용량이 충분하면 true
     */
    private Boolean isAvailable;
    
    /**
     * 동일 자재 여부 (계산 필드)
     * 현재 자재와 적재하려는 자재가 같으면 true
     */
    private Boolean isSameMaterial;
    
    /**
     * 분할 적재 수량 (입력 필드)
     * 용량 초과 시 분할하여 적재할 수량
     */
    private Integer allocateQty;
    
    /**
     * 구역 표시명 (계산 필드)
     * 행+열 조합 (예: A1, B2, C3...)
     */
    private String displayName;
    
    /**
     * 창고구역 상태 메시지 (계산 필드)
     * 적재 가능/불가능 등의 상태 메시지
     */
    private String areaStatusMessage;
    
    /**
     * 구역 선택 우선순위 (계산 필드)
     * 동일 자재 > 빈 구역 > 용량 여유 순으로 정렬용
     */
    private Integer priority;

    // ========== 편의 메서드들 ==========
    
    /**
     * 구역 표시명 자동 생성
     * areaRow + areaCol 조합 (예: A1, B2)
     */
    public void generateDisplayName() {
        if (this.areaRow != null && this.areaCol != null) {
            this.displayName = this.areaRow + this.areaCol.intValue();
        }
    }
    
    /**
     * 잔여 용량 자동 계산
     * vol - currentVolume
     */
    public void calculateAvailableVolume() {
        if (this.vol != null && this.currentVolume != null) {
            this.availableVolume = this.vol.intValue() - this.currentVolume;
        } else if (this.vol != null) {
            this.availableVolume = this.vol.intValue();
        } else {
            this.availableVolume = 0;
        }
    }
    
    /**
     * 적재 가능 여부 자동 판단
     * @param targetMcode 적재하려는 자재코드
     * @param targetQty 적재하려는 수량
     */
    public void checkAvailability(String targetMcode, Integer targetQty) {
        // 현재 자재가 없거나 동일한 자재인 경우
        boolean materialOk = (this.currentMaterial == null || this.currentMaterial.equals(targetMcode));
        
        // 용량이 충분한 경우
        boolean volumeOk = (this.availableVolume != null && targetQty != null && this.availableVolume >= targetQty);
        
        this.isAvailable = materialOk && volumeOk;
        this.isSameMaterial = (this.currentMaterial != null && this.currentMaterial.equals(targetMcode));
        
        // 상태 메시지 생성
        if (!materialOk) {
            this.areaStatusMessage = "다른 자재 적재중";
        } else if (!volumeOk) {
            this.areaStatusMessage = "용량 부족";
        } else if (this.isSameMaterial) {
            this.areaStatusMessage = "동일 자재 적재 가능";
        } else {
            this.areaStatusMessage = "적재 가능";
        }
        
        // 우선순위 설정 (동일 자재 > 빈 구역 > 용량 여유 순)
        if (this.isSameMaterial) {
            this.priority = 1;
        } else if (this.currentMaterial == null) {
            this.priority = 2;
        } else if (this.isAvailable) {
            this.priority = 3;
        } else {
            this.priority = 9;
        }
    }
    
    /**
     * 모든 계산 필드를 한번에 업데이트
     * @param targetMcode 적재하려는 자재코드
     * @param targetQty 적재하려는 수량
     */
    public void updateCalculatedFields(String targetMcode, Integer targetQty) {
        generateDisplayName();
        calculateAvailableVolume();
        checkAvailability(targetMcode, targetQty);
    }
}