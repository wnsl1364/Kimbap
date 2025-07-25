package com.kimbap.kbs.simjaejine.service;

import java.sql.Timestamp;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberAddVO {
  
  // 회원테이블
  private String memCd; // 회원코드
  private String id; // 아이디
  private String pw; // 비번
  private Timestamp lDate; // 최근로그인날짜
  private Date regDt; // 등록날짜
  private String memType; // 회원유형
  private String empCd; // 사원코드
  private String cpCd; // 거래처코드
  private String isUsed; // 사용여부
}
