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
public class ProdRequestDetailVO {
  private String produProdCd;     // 생산제품코드(PK)
  private String pcode;           // 제품마스터코드(FK)
  private String prodVerCd;       // 제품버전코드(FK)
  private String produReqCd;      // 생산요청번호(FK)
  private Integer reqQty;         // 요청수량
  private Integer seq;            // 요청순서
  @JsonFormat(pattern = "yyyy-MM-dd")
  private Date exProduDt;         // 생산요청일자
  
  // 검색 호출용
  private String unit;            // 단위
  private String prodName;        // 제품명
}
