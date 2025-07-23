package com.kimbap.kbs.materials.service;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 구매주문상세 테이블 VO
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PurcOrdDVO {
    private String purcDCd;         // 구매상세코드
    private String purcCd;          // 구매코드
    private String cpCd;            // 회사코드
    private String mcode;           // 자재코드
    private String mateVerCd;       // 자재버전코드
    private Integer purcQty;        // 구매수량
    private String unit;            // 단위
    private Double unitPrice;       // 단가
    private Date exDeliDt;          // 예상납기일자
    private String note;            // 비고
    private String purcDStatus;     // 구매상세상태
}
