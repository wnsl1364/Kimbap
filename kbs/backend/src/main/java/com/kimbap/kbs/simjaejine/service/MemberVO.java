package com.kimbap.kbs.simjaejine.service;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberVO {
  private String memCd;
  private String id;
  private String pw;
  private String memType;
  private String isUsed;
  private String empCd;
  private String empName;
  private String tel;
  private String teamName;
  private String deptName;
  private String idUsed;

  // 거래처 정보
  private String cpCd;
  private String cpName;
  private String cpType;
  private String repname;
  private String crnumber;
  private String cpTel;
  private String cpEmail;
  private String faxNum;
  private int loanTerm;
  private String mName;
  private String address;
  private String cpisUsed;
  private String chaRea;
  private String regDt;
  private String note;
  private String regi;
  private String modi;

  // 권한
  private List<String> authorities;
}
