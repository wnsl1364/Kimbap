package com.kimbap.kbs.simjaejine.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberVO {
  private String id;
  private String pw;
  private String empName;
  private String tel;
}
