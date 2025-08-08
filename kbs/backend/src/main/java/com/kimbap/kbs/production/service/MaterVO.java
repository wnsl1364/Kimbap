package com.kimbap.kbs.production.service;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MaterVO {
  private String mcode;           // 자재마스터코드
  private String mateVerCd;       // 자재버전코드
  private String mateName;        // 자재명
  private String mateType;        // 자재타입
  private String stoCon;          // 보관조건
  private String unit;            // 단위
  private String std;             // 규격
  private String pieceUnit;       // 낱개단위
  private BigDecimal converQty;   // 환산수량
  private BigDecimal moqty;       // 최소발주단위
  private BigDecimal safeStock;   // 안전재고
  private String edate;           // 소비기한
  private String corigin;         // 원산지
  private String isUsed;          // 사용여부
  private String regi;            // 등록자
  private String modi;            // 수정자
  private String chaRea;          // 변경사유
  private String regDt;           // 등록일자
  private String note;            // 비고
}
