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
public class MateSupplierVO {
  private String mateCpCd;        // 자재별 공급사 PK
  private String mcode;           // 자재마스터코드
  private String mateVerCd;       // 자재버전코드
  private String cpCd;            // 업체 PK
  private BigDecimal unitPrice;   // 단가
  private Integer ltime;          // 리드타임
  private String cpName;          // 업체명 (조인으로 가져올 예정)
}
