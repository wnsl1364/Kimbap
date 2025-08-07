package com.kimbap.kbs.production.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PurcOrdDetailVO {
  private String purcDCd;         // 발주상세코드
  private String purcCd;          // 발주서코드(FK)
  private String mcode;           // 자재코드
  private String mateVerCd;       // 자재버전코드
  private BigDecimal purcQty;     // 주문수량
  private String unit;            // 단위
  private BigDecimal unitPrice;   // 단가
  private LocalDate exDeliDt;     // 납기예정일
  private String note;            // 비고
  private String purcDStatus;     // 발주상태
  private String mateCpCd;        // 자재별 공급사 코드(FK)
  private LocalDate deliDt;       // 납기일자
  private BigDecimal currQty;     // 현재 납기수량

  // JOIN용
  private String mateName;        // 자재명
  private String supplierName;    // 공급업체명  
  private Integer leadTime;       // 리드타임
  private BigDecimal totalAmount; // 총액
}
