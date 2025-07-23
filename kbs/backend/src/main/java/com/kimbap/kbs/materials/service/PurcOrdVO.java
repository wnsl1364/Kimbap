package com.kimbap.kbs.materials.service;
// 구매주문 테이블 VO

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PurcOrdVO {
    private String purcCd;          // 구매코드
    private Date ordDt;             // 주문일자
    private String regi;            // 등록자
    private String purcStatus;      // 구매상태
    private Double ordTotalAmount;  // 주문총금액
}