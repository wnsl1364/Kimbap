package com.kimbap.kbs.payment.service;

import lombok.Data;

@Data
public class UnpaidSettleVO {
    private String statementCd;     // 입금내역 코드
    private String cpCd;            // 거래처 코드
    private int depositAmount;      // 입금 금액
}
