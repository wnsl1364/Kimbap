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
public class ProdInboundVO {
  private String prodInboCd;    // 제품입고코드
  private String pcode;         // 제품마스터코드
  private String prodVerCd;     // 제품버전코드
  private String lotNo;         // Lot 번호
  private String inboStatus;    // 입고상태
  private Integer inboQty;      // 입고수량
  private String produProdCd;   // 생산제품코드
  @JsonFormat(pattern = "yyyy-MM-dd")
  private Date inboDt;          // 입고일시
}
