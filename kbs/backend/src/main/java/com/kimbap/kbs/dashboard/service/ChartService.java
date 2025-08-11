package com.kimbap.kbs.dashboard.service;

import java.util.List;

public interface ChartService {
  // 대쉬보드 상단 건수 조회
  ChartVO getChartData();

  // 대시보드 PieChart 데이터 조회
  List<ChartVO> getPieData();

  // 바 차트 월별 매출 데이터
  List<ChartVO> getBarData();

  // 금일 주문요청 목록 데이터
  List<ChartVO> getOrderData();

}