package com.kimbap.kbs.distribution.service;

import lombok.Data;

@Data
public class RelDetailVO {
    private String pcode;       // 제품코드
    private String prodName;    // 제품명
    private Integer ordQty;     // 주문요청수량 (order_d 기준)
    private Integer relOrdQty;  // 출고지시수량 (release_ord 기준)
}
