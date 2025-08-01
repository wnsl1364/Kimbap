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
public class MateReleaseVO {
  private String mateRelCd;       // 자재출고코드
  private String produProdCd;     // 생산제품코드
  private String mcode;           // 자재마스터코드
  private String mateVerCd;       // 자재버전코드
  private String wslcode;         // 창고재고목록코드
  private String lotNo;           // Lot 번호
  private Integer relQty;         // 출고수량
  private String unit;            // 단위
  @JsonFormat(pattern = "yyyy-MM-dd")
  private Date relDt;             // 출고일자
  private String relType;         // 출고유형
  private String mname;           // 담당자
  private String note;            // 비고
  @JsonFormat(pattern = "yyyy-MM-dd")
  private Date creDt;             // 생성일시
  @JsonFormat(pattern = "yyyy-MM-dd")
  private Date modDt;             // 수정일시
}
