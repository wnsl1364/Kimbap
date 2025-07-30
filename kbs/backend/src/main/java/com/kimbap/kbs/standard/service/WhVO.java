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
public class WhVO {
    private String wcode; //창고코드
    private String wareVerCd; //창고 버전코드
    private String wareName; //창고명
    private String wareType; //창고 유형
    private String address; // 주소
    private Integer  maxRow; // 최대 행
    private Integer  maxCol; // 최대 열
    private Integer  maxFloor; // 최대 층
    private String isUsed; // 사용여부 
    private String chaRea; // 변경사유
    private String regi;     // 등록자
    private String modi;     // 수정자
    private Timestamp regDt; // 등록일자
    private String note; //비고

    private String fcode; //공장 코드
    private String facVerCd; //공장 버전 코드
     private String facName; // ✅ 공장명 추가
    
}
