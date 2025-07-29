package com.kimbap.kbs.materials.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchCriteria {
    // 검색 조건들
    private String purcCd;
    private String purcDCd;
    private String mateName;
    private String mcode;
    private String purcDStatus;
    private String purcStatus;
    private String cpCd;
    private String mateType;
    private String cpName;
    
    // 날짜 범위들
    private String startDate;
    private String endDate;
    private String exDeliStartDate;
    private String exDeliEndDate;
    private String deliStartDate;
    private String deliEndDate;
    
    // 권한 정보
    private String memtype;
    private String userId;
}