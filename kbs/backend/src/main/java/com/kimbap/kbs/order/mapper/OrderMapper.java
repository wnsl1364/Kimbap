package com.kimbap.kbs.order.mapper;

import java.util.List;

import com.kimbap.kbs.order.service.OrderVO;

public interface OrderMapper {
  List<OrderVO> getOrderList(); // 주문 목록 조회
  void insertOrder(OrderVO order); // 주문 등록 
}
