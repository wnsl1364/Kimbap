package com.kimbap.kbs.product.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 제품 정보 VO 클래스
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductVO {
  private String pcode;            // 제품코드
  private String prodVerCd;        // 제품버전코드
  private String prodName;         // 제품명
  private Double prodUnitPrice;    // 단가
}
