package com.kimbap.kbs.materials.service;

import java.math.BigDecimal;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 🎯 발주 조회 전용 VO - 필요한 것만 담자!
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseOrderViewVO {
    
    // 🎯 기본 식별자
    private String id;                    // UI용 고유 ID
    
    // 🎯 발주 기본 정보 (purc_ord 테이블)
    private String purcCd;               // 발주번호
    private Date ordDt;                  // 주문일자
    private String regi;                 // 등록자코드
    private String regiName;             // 등록자명
    private String purcStatus;           // 발주상태
    private BigDecimal ordTotalAmount;   // 발주총금액
    
    // 🎯 발주 상세 정보 (purc_ord_d 테이블)
    private String purcDCd;              // 발주상세번호
    private String mcode;                // 자재코드
    private String mateVerCd;            // 자재버전코드
    private Integer purcQty;             // 발주수량
    private String unit;                 // 단위
    private BigDecimal unitPrice;        // 단가
    private Date exDeliDt;              // 예상납기일
    private String purcDStatus;          // 발주상세상태
    private String note;                 // 비고
    
    // 🎯 자재 정보 (material 테이블)
    private String mateName;             // 자재명
    private String mateType;             // 자재타입
    
    // 🎯 거래처 정보 (company 테이블)
    private String cpCd;                 // 거래처코드
    private String cpName;               // 거래처명
    
    // 🎯 계산된 필드
    private BigDecimal totalAmount;      // 총액 (수량 * 단가)
    
    // 🎯 권한별 추가 필드 (p1 내부직원용)
    private Date deliDt;                 // 실제납기일 (입고테이블에서)
    
    // ❌ 이런 건 조회에서 필요 없어!
    // 창고, 입고, 출고, 반품, 재고 관련 필드들 전부 제외!
}