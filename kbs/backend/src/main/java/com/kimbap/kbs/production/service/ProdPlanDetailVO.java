package com.kimbap.kbs.production.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProdPlanDetailVO {
  private String ppdcode;     // 생산계획상세코드(PK)
  private String produPlanCd; // 생산계획코드(FK)
  private String pcode;       // 제품코드
  private String prodVerCd;   // 제품버전
  private Integer planQty;    // 계획수량
  private String unit;        // 단위
}
