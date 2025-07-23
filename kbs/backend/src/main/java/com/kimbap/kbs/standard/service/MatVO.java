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
public class MatVO {
    private String mcode;        // 자재마스터코드
    private String mateVerCd;    // 자재버전코드
    private String mateName;     // 자재명
    private String mateType;
    private String stoCon;
    private String unit;
    private String std;
    private String pieceUnit;
    private Double converQty;
    private Double moqty;
    private Double safeStock;
    private int edate;
    private String corigin;
    private String isUsed;
    private String regi;
    private String modi;
    private String chaRea;
    private Timestamp regDt;
    private String note;

    private List<MatSupplierVO> suppliers; // 자재별 공급사 리스트
}
