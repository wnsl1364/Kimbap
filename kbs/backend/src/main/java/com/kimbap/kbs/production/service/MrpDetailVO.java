package com.kimbap.kbs.production.service;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MrpDetailVO {
  private String mrpDCd;          // MRP 상세코드 (필요 시 GUID 자동 생성)
  private String mrpCd;           // 참조 MRP 코드
  private String mcode;           // 자재 코드
  private String mateVerCd;       // 자재 버전
  private String unit;            // 단위
  private BigDecimal requiredQty; // 부족 수량
  private String field;           // 생성 사유 (예: 생산계획)

  // JOIN
  private String mateName;        // 자재명
}
