package com.kimbap.kbs.standard.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MatSupplierVO {
    private String mateCpCd;   // 자재별공급사코드
    private String mcode;      // 자재코드
    private String mateVerCd;  // 자재버전코드
    private String cpCd;       // 거래처코드
    private String cpName;     // 거래처이름 
    private Integer unitPrice; // ✅ null 허용 가능성 고려해서 int → Integer
    private Integer ltime;     // ✅ same
    private String isUsed; // 사용여부
}
