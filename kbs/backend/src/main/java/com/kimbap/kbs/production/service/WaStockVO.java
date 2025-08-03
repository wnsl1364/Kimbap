package com.kimbap.kbs.production.service;

import java.math.BigDecimal;
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
public class WaStockVO {
  private String wslcode;       // 창고재고목록코드
  private String wareAreaCd;    // 창고구역코드
  private String mateInboCd;    // 자재입고코드
  private String prodInboCd;    // 제품입고코드
  private String itemType;      // 품목유형
  @JsonFormat(pattern = "yyyy-MM-dd")
  private Date inboDt;          // 입고일
  private String regi;          // 등록자
  private BigDecimal  qty;      // 수량
  private String unit;          // 단위

  private String lotNo;         // 자재입고 LOT번호(JOIN)
}
