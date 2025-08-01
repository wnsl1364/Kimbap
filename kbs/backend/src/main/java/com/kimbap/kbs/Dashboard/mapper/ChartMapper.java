package com.kimbap.kbs.dashboard.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.kimbap.kbs.dashboard.service.ChartVO;

@Mapper
public interface ChartMapper {

  // 대시보드 상단 건수 조회
  public ChartVO getChartData();

  // 대시보드 PieChart 데이터 조회
  public List<ChartVO> getPieData();

  // 바 차트 월별 매출 데이터
  public List<ChartVO> getBarData();

  // 금일 주문요청 목록 데이터
  public List<ChartVO> getOrderData();
  

}
