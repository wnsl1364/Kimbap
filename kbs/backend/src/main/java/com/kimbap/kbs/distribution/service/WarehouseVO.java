package com.kimbap.kbs.distribution.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseVO {
  private String wcode;       // 창고코드
  private String wareName;    // 창고이름
  private String lotNo;       // Lot번호
}
