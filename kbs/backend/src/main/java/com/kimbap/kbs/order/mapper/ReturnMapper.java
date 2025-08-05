package com.kimbap.kbs.order.mapper;

import java.util.List;
import java.util.Map;

import com.kimbap.kbs.order.service.ReturnItemVO;

public interface ReturnMapper {

  // 1. 반품 등록 (prod_return insert)
  int insertReturnItem(ReturnItemVO item);

  // 2. 반품 PK 시퀀스 번호 조회
  int getNextReturnCodeSeq();

  // 3. 주문 상세 상태 변경 → 't4' (반품요청)
  void updateOrderDetailStatus(String ordDCd);

  // 4. 주문 마스터 상태 변경 → 부분반품(s5) / 반품완료(s6)
  int updateOrderStatusCustomer(Map<String, String> params);

  // 5. 반품 이력 조회 (주문상세 기준으로 조회할 경우는 추가로 만들 수 있음)
  List<ReturnItemVO> getReturnHistoryByOrdCd(String ordCd);

  // 6. 주문 상세 전체 건수 조회
  int getOrderDetailCount(String ordCd);

  // 7. 주문 상세 특정 상태 건수 조회 (t5 등)
  int getOrderDetailStatusCount(String ordCd, String status);

  // 8. 반품 목록 조회
  List<ReturnItemVO> getReturnList(Map<String, Object> params);

  // 9. 반품 상태 업데이트 (승인/거절 공통)
  void updateReturnStatus(Map<String, Object> params);

  // 10. 주문상세 상태 t5(반품완료)로 변경
  void updateOrderDetailStatusToT5(String ordDCd);

  // 11. 주문상세 상태 t1(주문접수)로 복구 (거절 시)
  void updateOrderDetailStatusToT1(String prodReturnCd);

  // 12. prod_return_cd를 통해 주문코드(ord_cd) 조회
  String getOrdCdByReturnCd(String prodReturnCd);
}