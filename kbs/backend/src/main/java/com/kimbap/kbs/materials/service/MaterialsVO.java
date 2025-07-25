package com.kimbap.kbs.materials.service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 자재 관리 통합 VO - 모든 자재 관련 테이블을 하나로!
 * 발주주문, 발주상세, 입고, 출고, 반품 전부 다 여기에!
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MaterialsVO {
    
    // ========== 공통 필드 (모든 권한) ==========
    private String purcCd;          // 발주코드
    private String mateName;        // 자재명
    private Integer purcQty;        // 발주수량
    private Date ordDt;             // 주문일자
    
    // ========== p1 권한 추가 필드 ==========
    private String purcDCd;         // 발주상세코드
    private String mateType;        // 자재타입
    private String unit;            // 단위
    private Date exDeliDt;          // 예상납기일자
    private String purcDStatus;     // 발주상세상태
    private String note;            // 비고
    
    // ========== p3 권한 추가 필드 ==========
    private String mcode;           // 자재코드
    private BigDecimal totalAmount; // 총금액 (계산 필드)
    
    // ========== 자재입고 (MateInboVO) ==========
    private String mateInboCd;      // 자재입고코드
    private String wcode;           // 창고코드
    private String wareVerCd;       // 창고버전코드
    private String lotNo;           // 로트번호
    private String supplierLotNo;   // 공급업체로트번호
    private Date inboDt;            // 입고일자
    private String inboStatus;      // 입고상태
    private Integer totalQty;       // 총수량
    private String mname;           // 자재명
    private Date deliDt;            // 납기일자
    private String cpName;          // 회사명  

    
    // ========== 자재출고 (MateRelVO) ==========
    private String mateRelCd;       // 자재출고코드
    private String produProdCd;     // 제품생산코드
    private String wslcode;         // 작업지시코드
    private Integer relQty;         // 출고수량
    private Date relDt;             // 출고일자
    private String relType;         // 출고유형
    private Timestamp creDt;        // 생성일시
    private Timestamp modDt;        // 수정일시
    
    // ========== 자재반품 (MateReturnVO) ==========
    private String mateReturnCd;    // 자재반품코드
    private Date returnDt;          // 반품일자
    private Integer returnQty;      // 반품수량
    private Double returnAmount;    // 반품금액
    private String returnRea;       // 반품사유
    
    // ========== 추가 편의 필드들 ==========
    private String currentStatus;   // 현재 전체 상태
    private Integer currentStock;   // 현재 재고량
    private Double totalProcessedAmount; // 총 처리 금액
    
    // ========== 편의 메서드들 ==========
    
    /**
     * 현재 재고량 자동 계산해주는 메서드
     */
    public void calculateAndSetCurrentStock() {
        int inbound = totalQty != null ? totalQty : 0;
        int released = relQty != null ? relQty : 0;
        int returned = returnQty != null ? returnQty : 0;
        
        this.currentStock = inbound - released + returned;
    }
    
    /**
     * 전체 상태 요약해주는 메서드
     */
    public void generateStatusSummary() {
        StringBuilder status = new StringBuilder();
        
        if (purcDStatus != null && !purcDStatus.isEmpty()) {
            status.append("발주:").append(purcDStatus);
        }
        if (inboStatus != null && !inboStatus.isEmpty()) {
            if (status.length() > 0) status.append(" | ");
            status.append("입고:").append(inboStatus);
        }
        if (relType != null && !relType.isEmpty()) {
            if (status.length() > 0) status.append(" | ");
            status.append("출고:").append(relType);
        }
        if (returnRea != null && !returnRea.isEmpty()) {
            if (status.length() > 0) status.append(" | ");
            status.append("반품:").append(returnRea);
        }
        
        this.currentStatus = status.toString();
    }
    
    /**
     * 총 처리 금액 계산해주는 메서드
     */
    public void calculateTotalAmount() {
        double orderAmount = totalAmount != null ? totalAmount.doubleValue() : 0.0;
        double returnAmount = this.returnAmount != null ? this.returnAmount : 0.0;
        
        this.totalProcessedAmount = orderAmount - returnAmount;
    }
    
    /**
     * 모든 계산 한번에 해주는 메서드 - 이거 하나만 호출하면 끝!
     */
    public void calculateAll() {
        calculateAndSetCurrentStock();
        generateStatusSummary();
        calculateTotalAmount();
    }
}