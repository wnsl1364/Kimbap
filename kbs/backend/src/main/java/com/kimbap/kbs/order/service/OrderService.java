package com.kimbap.kbs.order.service;

import java.util.List;
import java.util.Map;

public interface  OrderService {
  // 주문 등록
  void registerOrder(OrderVO orderVO);

  // 주문 비활성화
  void deactivateOrder(String ordCd);

  // 주문 목록 조회
  List<OrderVO> getOrderList(Map<String, Object> params);

  // 주문 상세 조회 (상세 정보 포함)
  OrderVO getOrderWithDetails(String ordCd);

  // 주문 수정 (기존 상세는 update, 신규는 시퀀스로 insert, 삭제된 상세는 delete)
  void updateOrder(OrderVO orderVO);

}
