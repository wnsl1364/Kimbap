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
  private String prodName;    // 제품코드 + 제품버전 제품기준정보 JOIN
  private Integer planQty;    // 계획수량
  private String unit;        // 단위
}
