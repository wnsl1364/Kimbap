package com.kimbap.kbs.production.service;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

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
  @JsonFormat(pattern = "yyyy-MM-dd")
  private Date exProduDt;     // 생산예정일자
  private Integer seq;        // 우선순위

  // 생산 요청 시 사용 VO
  private Integer totalReqQty;    // 기요청수량
  private Integer remainingQty;   // 계획수량 - 요청수량 = 잔존수량
}
