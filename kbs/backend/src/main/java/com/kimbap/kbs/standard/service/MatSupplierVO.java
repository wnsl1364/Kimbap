package com.kimbap.kbs.standard.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MatSupplierVO {
    private String mateCpCd;   // 자재별공급사코드
    private String mcode;       // 자재코드
    private String mateVerCd;   // 자재버전코드
    private String cpCd;        // 거래처코드
    private BigDecimal unitPrice; // 공급가격
    private Integer ltime;      // 리드타임
}
