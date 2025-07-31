package com.kimbap.kbs.production.service;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProdRequestFullVO {
  private ProdRequestVO request;
  private List<ProdRequestDetailVO> reqDetails;
}
