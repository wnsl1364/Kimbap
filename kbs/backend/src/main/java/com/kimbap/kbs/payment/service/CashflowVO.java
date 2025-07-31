package com.kimbap.kbs.payment.service;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CashflowVO {
    private String statementCd;    // 입출금내역코드
    private String transType;      // 거래유형
    private String depo;           // 입금자명
    private Integer depositAmount; // 금액
    private String bankName;       // 은행명
    private String regi;           // 등록자
    private String modi;           // 수정자
    private Timestamp regDt;       // 등록일자
    private String note;           //비고
}
