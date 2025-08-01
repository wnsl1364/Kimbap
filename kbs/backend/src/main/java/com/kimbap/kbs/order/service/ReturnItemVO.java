package com.kimbap.kbs.order.service;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 반품을 위한 VO 클래스
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReturnItemVO {
     // 기본 정보
    private String prodReturnCd;  // 반품 코드 (PK)
    private String ordCd;         // 주문 코드
    private String ordDCd;        // 주문 상세 코드
    private String pcode;         // 제품 코드
    private String prodVerCd;     // 제품 버전 코드

    // 반품 정보
    private Integer  returnQty;        // 반품 수량
    private String returnRea;     // 반품 사유
    private Date returnDt;      // 반품 일자
    private Integer  returnAmount;     // 반품 금액
    private String lotNo;         // LOT 번호
}
