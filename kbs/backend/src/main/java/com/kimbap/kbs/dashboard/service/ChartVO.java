package com.kimbap.kbs.dashboard.service;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChartVO {
  
  // 대시보드 상단 card 데이터
  private int prodInbo;
  private int prodReturn;
  private int releaseOrd;
  private int prodRel;

  // 대시보드 PieChart 데이터
  private String prodName;
  private int pieTotalQty;

  // 대시보드 BarChart 데이터
  private String month;
  private int totalSales;
  
  // 대시보드 금일 요청주문 데이터
  private Date ordDt;
  private String cpName;
  private int ordTotalQty;
  private Date deliAvailDt;
  private String ordDStatus;

}
