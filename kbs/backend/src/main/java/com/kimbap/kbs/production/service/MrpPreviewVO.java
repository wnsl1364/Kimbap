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
public class MrpPreviewVO {
  private List<MrpDetailVO> mrpDetails;                    // 기존 MrpDetailVO 재사용
  private List<PurcOrdDetailVO> purchaseOrderDetails;      // 기존 PurchaseOrderDetailVO 재사용
  private String previewMrpCd;                             // 미리보기용 가상 MRP 코드
  private String previewPurcCd;                            // 미리보기용 가상 발주 코드
}
