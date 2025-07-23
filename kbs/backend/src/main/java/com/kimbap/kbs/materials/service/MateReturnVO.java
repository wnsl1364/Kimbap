package com.kimbap.kbs.materials.service;
// 자재 반품 테이블 VO

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MateReturnVO {
    private String mateReturnCd;    // 자재반품코드
    private String purcCd;          // 구매코드
    private String lotNo;           // 로트번호
    private Date returnDt;          // 반품일자
    private Integer returnQty;      // 반품수량
    private Double returnAmount;    // 반품금액
    private String returnRea;       // 반품사유
}