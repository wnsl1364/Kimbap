package com.kimbap.kbs.production.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProdPlanDetailVO {
  private String ppdcode;
  private String produPlanCd;
  private String pcode;
  private String prodVerCd;
  private Integer planQty;
}
