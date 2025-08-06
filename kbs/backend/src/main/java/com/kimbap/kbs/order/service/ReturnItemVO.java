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
    private String ordDCd;        // 주문 상세 코드
    private String pcode;         // 제품 코드
    private String prodVerCd;     // 제품 버전 코드

    // 반품 정보
    private Integer  returnQty;        // 반품 수량
    private String returnRea;     // 반품 요청 사유
    private String rejectRea; // 반품 거절 사유
    private Date returnDt;      // 반품 일자
    private Integer  returnAmount;     // 반품 금액
    private String lotNo;         // LOT 번호
    private String returnStatusCustomer; //반품상태(매출업체)
    private String returnStatusInternal; //반품상태(내부)

    private String cpName;
    private String prodName;
    private Date returnEndDt;
    private String manager;
    private String managerName;
}
