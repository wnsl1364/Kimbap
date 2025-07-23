package com.kimbap.kbs.materials.service;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 자재입고 테이블 VO
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MateInboVO {
    private String mateInboCd;      // 자재입고코드
    private String mcode;           // 자재코드
    private String mateVerCd;       // 자재버전코드
    private String wcode;           // 창고코드
    private String wareVerCd;       // 창고버전코드
    private String purcDCd;         // 구매상세코드
    private String lotNo;           // 로트번호
    private String supplierLotNo;   // 공급업체로트번호
    private Date inboDt;            // 입고일자
    private String inboStatus;      // 입고상태
    private Integer totalQty;       // 총수량
    private String mname;           // 자재명
    private String note;            // 비고
    private String cpCd;            // 회사코드
    private Date deliDt;            // 납기일자
}