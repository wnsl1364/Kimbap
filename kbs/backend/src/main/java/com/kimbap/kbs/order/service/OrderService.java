package com.kimbap.kbs.order.service;

import java.util.List;

public interface  OrderService {
  // 주문 등록
  void registerOrder(OrderVO orderVO);

  // 주문 목록 조회
  List<OrderVO> getOrderList();
}
