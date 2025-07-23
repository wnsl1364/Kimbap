package com.kimbap.kbs.standard.service;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompanyVO {
    private String cpCd; // 거래처코드
    private String cpName; // 거래처명 
    private String cpType; // 거래처 유형
    private String repname; // 대표자명
    private String crnumber; // 사업자번호
    private String tel; // 전화번호
    private String cpEmail; // 이메일
    private String faxNum; // 팩스번호
    private int loanTerm; // 여신기간
    private String mname; // 담당자명
    private String address; // 주소
    private String isUsed; // 사용여부
    private String chaRea; // 변경사유
    private Timestamp regDt; // 등록일자
    private String note; //비고
}
