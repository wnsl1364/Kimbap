package com.kimbap.kbs.standard.service;

import java.sql.Timestamp;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FacVO {
    private String fcode; // 공장코드
    private String facVerCd; // 공장 버전 코드
    private String facName; // 공장명
    private String address; // 주소
    private String tel; // 연락처
    private String mname; // 담당자명
    private String opStatus; // 가동상태
    private String chaRea; // 변경사유
    private Timestamp regDt; // 등록일자
    private String note; // 비고
    private String regi; // 등록자
    private String modi; // 수정자
    private List<FacMaxVO> facMaxList; // 공장별 최대 생산량 
}
