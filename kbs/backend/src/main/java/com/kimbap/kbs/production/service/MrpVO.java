package com.kimbap.kbs.production.service;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MrpVO {
  private String mrpCd;           // MRP 코드
  private LocalDate planGeneDt;   // 계획 생성일
  private LocalDate produStartDt; // 생산 시작일
  private String note;            // 비고
  private String regi;            // 등록자
  private String produPlanCd;     // 참조 생산계획번호
}
