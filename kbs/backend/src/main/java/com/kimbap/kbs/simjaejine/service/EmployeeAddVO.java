package com.kimbap.kbs.simjaejine.service;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeAddVO {
  private String empCd; // 사원코드
  private String empName; // 사원이름
  private Date birt; // 생년월일
  private String tel; // 연락처
  private String email; // 이메일
  private String address; // 주소
  private Date hDate; // 입사날짜
  private Date leDate; // 퇴사날짜 
  private String teamCode; // 팀코드
  private String isUsed; // 사용여부
}
