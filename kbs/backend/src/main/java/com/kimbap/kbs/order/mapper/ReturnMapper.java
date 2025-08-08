package com.kimbap.kbs.order.mapper;

import java.util.List;
import java.util.Map;

import com.kimbap.kbs.order.service.ReturnItemVO;

public interface ReturnMapper {

  // 반품 등록 (prod_return insert)
  int insertReturnItem(ReturnItemVO item);

  // 반품 PK 시퀀스 번호 조회
  int getNextReturnCodeSeq();

  // 주문 상세 상태 변경 → 't4' (반품요청)
  void updateOrderDetailStatus(String ordDCd);

  // 주문 마스터 상태 변경 → 부분반품(s5) / 반품완료(s6)
  int updateOrderStatusCustomer(Map<String, String> params);

  // 반품 이력 조회 (주문상세 기준으로 조회할 경우는 추가로 만들 수 있음)
  List<ReturnItemVO> getReturnHistoryByOrdCd(String ordCd);

  // 주문 상세 전체 건수 조회
  int getOrderDetailCount(String ordCd);

  // 주문 상세 특정 상태 건수 조회 (t5 등)
  int getOrderDetailStatusCount(String ordCd, String status);

  // 반품 목록 조회
  List<ReturnItemVO> getReturnList(Map<String, Object> params);

  // 반품 상태 업데이트 (승인/거절 공통)
  void updateReturnStatus(Map<String, Object> params);

  // 주문상세 상태 t5(반품완료)로 변경
  void updateOrderDetailStatusToT5(String ordDCd);

  // 주문상세 상태 t1(주문접수)로 복구 (거절 시)
  void updateOrderDetailStatusToT1(String prodReturnCd);

  // prod_return_cd를 통해 주문코드(ord_cd) 조회
  String getOrdCdByReturnCd(String prodReturnCd);

  // lot번호
  String getLotNoByOrdDCd(String ordDCd);

  // 주문상세 상태를 출고완료(t3)로 변경
  void updateOrderDetailStatusToT3(String ordDCd);

  // prod_return 상태를 반품요청취소(v2)로 변경
  void updateProdReturnStatusToV2(String ordDCd);

  // 주문상세 코드로 주문코드 조회
  String getOrdCdByOrdDCd(String ordDCd);

  // return_amount 계산
  void updateReturnAmount(Map<String, Object> params);

  // 미수금 차감
  void decreaseCompanyUnsettledAmount(Map<String, Object> params);
}