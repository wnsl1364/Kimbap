package com.kimbap.kbs.distribution.service;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RelOrderAndResultVO {
  private Date relDt;         // 출고지시일자
  private String relMasCd;    // 출고지시마스터번호
  private String cpName;      // 거래처명
  private String prodName;    // 제품명
  private int relOrdQty;    // 출고지시수량
  private String deliAdd;     // 배송지주소
  private String relOrdStatus;   // 출고지시상태
  private String note;        // 비고
  private String ordQty; // 주문 수량 합계

  // 마스터테이블 단건조회 
  
  // 검색 VO
  private String type;          // 타입 (요청, 부분출고, 출고완료 등)
  private Date startDate;       // 시작일
  private Date endDate;         // 종료일
}
