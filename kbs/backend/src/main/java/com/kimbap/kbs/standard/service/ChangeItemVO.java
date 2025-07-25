package com.kimbap.kbs.standard.service;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChangeItemVO {
    private String fieldName;      // 변경된 항목명 (예: 자재명)
    private String oldValue;       // 이전 값
    private String newValue;       // 변경 후 값
    private String changeReason;   // 변경 사유 (chaRea)
    private String version;        // 버전 (mateVerCd)
    private Timestamp regDt;       // 수정일자
    private String modi;           // 수정자
}