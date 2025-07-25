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
public class ProdPlanVO {
  private String produPlanCd; // 생산계획코드(PK)
  private String fcode;       // 공장코드(FK)
  private String facVerCd;    // 공장버전(FK)
  @JsonFormat(pattern = "yyyy-MM-dd")
  private Date planDt;        // 계획수립일자
  @JsonFormat(pattern = "yyyy-MM-dd")
  private Date planStartDt;   // 계획시작일자
  @JsonFormat(pattern = "yyyy-MM-dd")
  private Date planEndDt;     // 계획종료일자
  private String mname;       // 담당자
  private String note;        // 비고

  // prod_plan 조건 검색을 위한 VO
  @JsonFormat(pattern = "yyyy-MM-dd")
  private Date planDtStart;
  @JsonFormat(pattern = "yyyy-MM-dd")
  private Date planDtEnd;
  @JsonFormat(pattern = "yyyy-MM-dd")
  private Date periodStartDt;
  @JsonFormat(pattern = "yyyy-MM-dd")
  private Date periodEndDt;
  private String facName; // fcode, facVerCd를 조합

  private Integer sumPlanQty;  // 생산계획코드를 참조하는 상세 계획수량 합계
  private String firstUnit;    // 생산계획상세 첫번쨰 제품 unit(단위)
}
