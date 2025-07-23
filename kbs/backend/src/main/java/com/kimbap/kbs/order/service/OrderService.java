package com.kimbap.kbs.order.service;

import java.util.List;

public interface  OrderService {
  List<OrderVO> getOrderList(); // 주문 목록 조회
  void insertOrder(OrderVO order); // 주문 등록
}
