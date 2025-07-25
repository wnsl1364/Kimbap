package com.kimbap.kbs.order.mapper;

import java.util.List;

import com.kimbap.kbs.order.service.OrderDetailVO;
import com.kimbap.kbs.order.service.OrderVO;

public interface OrderMapper {
  // 주문 마스터 등록
  int insertOrderMaster(OrderVO orderVO);

  // 주문 상세 등록
  int insertOrderDetail(OrderDetailVO detailVO);

  // 주문 상세 코드 자동 생성
  String getGeneratedOrderDetailCode();

  // 오늘 날짜 기준 가장 큰 주문 코드
  String getLatestOrderCode();

  // 주문 목록 조회
  List<OrderVO> getOrderList(); 
}
