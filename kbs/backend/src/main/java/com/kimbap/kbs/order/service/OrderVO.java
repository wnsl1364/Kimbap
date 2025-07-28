package com.kimbap.kbs.order.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 주문 목록 조회 및 주문 등록을 위한 VO 클래스
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderVO {
    private String ordCd;
    private String cpCd;
    private Date ordDt;
    private String note;
    private Date deliReqDt;
    private Date deliAvailDt;
    private String ordStatus;
    private String deliAdd;
    private String regi;
    private Date regDt;
    private BigDecimal ordTotalAmount;
    private BigDecimal curPayAmount;
    private Date exPayDt;
    private Date actPayDt;
    private String isUsed;
    
    // 뷰 전용 필드 추가
    private String prodName;           // 대표 제품명
    private BigDecimal totalQty;       // 총 주문 수량
    private BigDecimal returnQty;      // 총 반품 수량
    private BigDecimal totalAmount;    // 총 금액

    // 주문 상세 목록
    private List<OrderDetailVO> orderDetails;
}
