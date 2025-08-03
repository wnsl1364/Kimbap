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
public class BomDetailVO {
  private String bomDCd;      // BOM상세코드
  private BigDecimal needQty;    // 소요수량
  private String unit;        // 단위
  private String isUsed;      // 사용여부
  private String mcode;       // 자재마스터코드
  private String mateVerCd;   // 자재버전코드
  private String bcode;       // BOM마스터코드
  private String bomVerCd;    // BOM버전코드
}
