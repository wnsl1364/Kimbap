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
public class ProdRequestVO {
  private String produReqCd;          // 생산요청번호(PK)
  @JsonFormat(pattern = "yyyy-MM-dd")
  private Date reqDt;                 // 생산요청일자
  private String requ;                // 요청자
  private String note;                // 비고
  private String prReqStatus;         // 생산요청상태
  private String produPlanCd;         // 생산계획번호(FK)
  @JsonFormat(pattern = "yyyy-MM-dd")
  private Date deliDt;                // 납기일자
  private String empName;             // 사원이름

  // production_req 조건 검색을 위한 VO
  private String fcode;               // 공장코드(FK)
  private String facVerCd;            // 공장버전(FK)
  @JsonFormat(pattern = "yyyy-MM-dd")
  private Date reqDtStart;            // 요청일자 기간 시작
  @JsonFormat(pattern = "yyyy-MM-dd")
  private Date reqDtEnd;              // 요청일자 기간 끝
  private String facName;             // fcode, facVerCd를 조합

  private Integer sumReqQty;          // 생산계획코드를 참조하는 상세 계획수량 합계
  private String firstUnit;           // 생산계획상세 첫번쨰 제품 unit(단위)
}
