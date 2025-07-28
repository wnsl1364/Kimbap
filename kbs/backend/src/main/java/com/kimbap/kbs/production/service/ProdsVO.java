package com.kimbap.kbs.production.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProdsVO {
  private String pcode;         // 제품코드
  private String prodVerCd;     // 제품버전코드
  private String prodName;      // 제품명
  private Double prodUnitPrice; // 제품단가
  private String wei;           // 중량
  private String unit;          // 단위
  private Integer edate;        // 소비기한
  private String stoTemp;       // 보관온도
  private Integer safeStock;    // 안전재고
  private String pacUnit;       // 포장단위
  private String chaRea;        // 변경사유
  private String isUsed;        // 사용여부
  private String note;          // 비고
  private Double primeCost;     // 원가
  private String regi;          // 등록자
  private String modi;          // 수정자
}
