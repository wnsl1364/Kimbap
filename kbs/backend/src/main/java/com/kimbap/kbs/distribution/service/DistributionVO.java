package com.kimbap.kbs.distribution.service;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DistributionVO {
    private Date regDt;          // 입출고일자
    private String type;          // '입고' or '출고'
    private String pCode;         // 제품코드
    private String prodName;     // 제품명
    private int qty;              // 수량
    private String wareAreaCd;  // 창고
    private int stockQty;        // 잔여재고
    private String note;          // 비고
}
