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
public class PurcOrdVO {
  private String purcCd;              // 발주서코드(PK)
  private LocalDate ordDt;            // 발주일자
  private String regi;                // 등록자
  private String purcStatus;          // 발주서 상태
  private BigDecimal ordTotalAmount;  // 주문금액 총액
}
