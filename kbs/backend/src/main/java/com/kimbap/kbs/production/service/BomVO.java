package com.kimbap.kbs.production.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor 
@NoArgsConstructor
public class BomVO {
  private String bcode;       // BOM마스터코드
  private String bomVerCd;    // BOM버전코드
  private String pcode;       // 제품마스터코드
  private String prodVerCd;   // 제품버전코드
  private String regDt;       // 등록일자
  private String isUsed;      // 사용여부
}
