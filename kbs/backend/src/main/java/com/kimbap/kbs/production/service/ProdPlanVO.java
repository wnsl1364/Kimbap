package com.kimbap.kbs.production.service;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProdPlanVO {
  private String produPlanCd;
  private String fcode;
  private String facVerCd;
  private Date planStartDt;
  private Date planEndDt;
  private String mname;
}
