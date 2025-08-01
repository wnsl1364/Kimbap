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
public class MateInboundVO {
  private String mateInboCd;      // 자재입고코드
  private String mcode;           // 자재마스터코드
  private String mateVerCd;       // 자재버전코드
  private String wcode;           // 창고마스터코드
  private String wareVerCd;       // 창고버전코드
  private String purcDCd;         // 발주상세코드
  private String fcode;           // 공장마스터코드
  private String facVerCd;        // 공장버전코드
  private String lotNo;           // LOT번호
  private String supplierLotNo;   // 공급사 LOT번호
  @JsonFormat(pattern = "yyyy-MM-dd")
  private Date inboDt;            // 입고일자
  private String inboStatus;      // 입고상태
  private Integer totalQty;       // 총수량
  private String mname;           // 담당자
  private String note;            // 비고
  private String cpCd;            // 거래처코드
  @JsonFormat(pattern = "yyyy-MM-dd")
  private Date deliDt;            // 납기일
}
