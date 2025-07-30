package com.kimbap.kbs.standard.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WhDetailVO {
    private String wareAreaCd; // 창고구역코드
    private Integer areaRow; // 구역행
    private Integer areaCol; // 구역 열
    private Integer areaFloor; // 구역 층
    private String isUsed; // 사용여부
    private Integer vol; // 용량
    private String wcode; //창고 코드
    private String wareVerCd; // 창고 버전코드 
}
