package com.kimbap.kbs.standard.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FacMaxVO {
    private String maxProduCd; // 최대생산코드
    private String fcode; // 공장마스터코드
    private String facVerCd; // 공장버전코드
    private Double mpqty; // 최대생산량
    private String pcode; // 제품마스터코드
    private String prodName; // 제품명
    private String prodVerCd; // 제품버전코드

}
