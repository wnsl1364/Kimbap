package com.kimbap.kbs.order.mapper;

import java.util.List;
import java.util.Map;

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
  List<OrderVO> getOrderList(Map<String, Object> params);

  // 주문 비활성화
  void deactivateOrder(String ordCd);

  // 주문 마스터 단건 조회
  OrderVO selectOrder(String ordCd);

  // 주문 상세 목록 조회
  List<OrderDetailVO> selectOrderDetail(String ordCd);

  // 주문 마스터 수정
  int updateOrderMaster(OrderVO orderVO);

  // 주문 상세 전체 삭제
  void deleteOrderDetails(String ordCd);

  // 주문 상세 개별 삭제
  void deleteOrderDetailByOrdDCd(String ordDCd);

  // 주문 상세 시퀀스 번호 조회
  int getNextOrderDetailSeq();

  // 기존 상세 수정
  int updateOrderDetail(OrderDetailVO detail);

  // 주문 상태 수정
  void updateOrderStatus(OrderDetailVO detail);
}
