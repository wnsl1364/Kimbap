package com.kimbap.kbs.production.service;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FactoryVO {
    private String fcode; //  공장마스터코드
    private String facVerCd; // 공장버전코드
    private String facName; // 공장 이름
    private String address; // 공장 주소
    private String tel; // 공장 번호
    private Stirng mname; // 담당자명
    private String opStatus; // 가동상태
    private String chaRea; // 변경사유
    private String regDt; // 등록일자
    private String note; // 비고
}
