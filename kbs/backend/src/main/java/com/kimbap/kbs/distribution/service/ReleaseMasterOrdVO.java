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
public class ReleaseMasterOrdVO {
    private String relMasCd;     // 출고마스터코드
    private String regi;       // 등록자
    private Date relDt;         // 출고일자
    private String note;       // 비고
    private String cp_cd;      // 거래처코드
    private String mName;       // 거래처담당자
    private String deli_add; // 배송지주소
    private Date deli_req_dt; // 배송요청일자
    private String relOrdStatus; // 출고지시서 상태
}
