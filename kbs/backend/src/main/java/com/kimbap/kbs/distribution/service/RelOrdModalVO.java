package com.kimbap.kbs.distribution.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RelOrdModalVO {
  // 모달창
  private String ordCd;           // 주문코드
  private String cpName;          // 거래처명
  private String prodNameSummary; // 제품명 요약 ("참치김밥 외 1개")
  private String ordDt;           // 주문일자

  // 모달 선택시
  private String prodName;
  private String ordDCd;
  private int ordQty;
  private int relQty;
  private int noRelQty;
  private String relOrdStatus;
  private String mCpName;
  private String mName;
  private String newRelOrdCd;
  private String wcode;
  private String wareVerCd;
  
  
}
