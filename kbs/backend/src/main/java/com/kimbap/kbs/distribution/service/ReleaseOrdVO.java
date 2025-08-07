package com.kimbap.kbs.distribution.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReleaseOrdVO {
    // 출고지시서 테이블 등록
    private String newRelOrdCd;   // 출고지시서 코드
    private String wcode;         // 창고마스터코드
    private String wareVerCd;     // 창고버전코드
    private String ordDCd;        // 주문상세코드
    private int relQty;      // 출고수량
    private String relMasCd;   // 출고마스터코드

}
