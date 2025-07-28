package com.kimbap.kbs.standard.service;

import java.math.BigDecimal;
import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProdVO {
    private String pcode;           // 제품마스터코드
    private String prodVerCd;       // 제품버전코드
    private String prodName;        // 제품명
    private BigDecimal prodUnitPrice; // 제품단가
    private String wei;             // 중량 (공통코드)
    private String unit;            // 단위 (공통코드)
    private Integer edate;          // 소비기한 (일)
    private String stoTemp;         // 보관온도 (공통코드)
    private Integer safeStock;      // 안전재고
    private String pacUnit;         // 포장단위 (공통코드)
    private String chaRea;          // 변경사유
    private String isUsed;          // 사용여부 (공통코드)
    private String regi;            // 등록자
    private String modi;            // 수정자
    private Timestamp regDt;        // 등록일자
    private BigDecimal primeCost;   // 원가
    private String note;            // 비고
}
