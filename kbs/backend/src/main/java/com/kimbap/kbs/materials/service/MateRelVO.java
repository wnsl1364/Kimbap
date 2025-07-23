package com.kimbap.kbs.materials.service;

import java.sql.Timestamp;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 자재출고 테이블 VO
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MateRelVO {
    private String mateRelCd;       // 자재출고코드
    private String produProdCd;     // 제품생산코드
    private String mcode;           // 자재코드
    private String mateVerCd;       // 자재버전코드
    private String wslcode;         // 작업지시코드
    private String lotNo;           // 로트번호
    private Integer relQty;         // 출고수량
    private String unit;            // 단위
    private Date relDt;             // 출고일자
    private String relType;         // 출고유형
    private String mname;           // 자재명
    private String note;            // 비고
    private Timestamp creDt;        // 생성일시
    private Timestamp modDt;        // 수정일시
}