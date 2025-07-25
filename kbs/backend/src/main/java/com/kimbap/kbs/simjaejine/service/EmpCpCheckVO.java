package com.kimbap.kbs.simjaejine.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmpCpCheckVO {
  // 회원테이블
  private String empCd; // 사원코드
  private String empName; // 사원명

  // 거래처테이블
  private String cpCd; // 거래처코드
  private String cpName; // 거래처명
}
