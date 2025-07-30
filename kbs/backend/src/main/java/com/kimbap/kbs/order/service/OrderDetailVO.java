package com.kimbap.kbs.order.service;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 주문 상세 VO
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailVO {
    private String ordCd;       // 주문코드 (외래키)
    private String ordDCd; // 주문상세코드 (ORDD-2025-000001)
    private String pcode;       // 제품코드
    @JsonProperty("prodName")
    private String prodName;
    private String prodVerCd;   // 제품버전코드 (옵션)
    private Integer ordQty;       // 주문수량
    private BigDecimal unitPrice; // 단가
    private Date deliAvailDt;    // 납기 가능일자
    private String ordDStatus;   // 주문상세 상태 (예: t1)
    private String isUsed;
}
