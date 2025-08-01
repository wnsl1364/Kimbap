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
public class BomFullVO {
  private BomVO bom;
  private List<BomDetailVO> bomDetails;
}
