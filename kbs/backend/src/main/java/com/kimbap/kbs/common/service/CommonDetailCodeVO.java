package com.kimbap.kbs.common.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommonDetailCodeVO {
  private String dCd;
  private String cdInfo;
  private int seq;
  private String isUsed;
  private String groupCd;
}
