package com.kimbap.kbs.dashboard.serviceimpl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kimbap.kbs.dashboard.mapper.ChartMapper;
import com.kimbap.kbs.dashboard.service.ChartService;
import com.kimbap.kbs.dashboard.service.ChartVO;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class ChartServiceImpl implements ChartService{
  
      private final ChartMapper chartMapper;

    // 대시보드 상단 건수 조회
    @Override
    public ChartVO getChartData() {
        return chartMapper.getChartData();
    }

    // 대시보드 파이차트 데이터 조회
    @Override
    public List<ChartVO> getPieData() {
        return chartMapper.getPieData();
    }

    // 대시보드 바 차트 데이터 조회
    @Override
    public List<ChartVO> getBarData() {
        return chartMapper.getBarData();
    }

    // 금일 주문요청 목록 데이터
    @Override
    public List<ChartVO> getOrderData() {
        return chartMapper.getOrderData();
    }

}
