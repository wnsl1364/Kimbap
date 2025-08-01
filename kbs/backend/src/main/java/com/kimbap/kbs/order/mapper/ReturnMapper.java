package com.kimbap.kbs.order.mapper;

import java.util.List;

import com.kimbap.kbs.order.service.ReturnItemVO;

public interface ReturnMapper {

  // 1. 반품 등록 (prod_return insert)
  int insertReturnItem(ReturnItemVO item);

  // 2. 반품 PK 시퀀스 번호 조회 (ex: SEQ_PROD_RETURN.NEXTVAL)
  String getNextReturnCodeSeq();

  // 3. 주문 상세 상태 변경 → 't4' (반품요청)
  void updateOrderDetailStatus(String ordDCd);

  // 4. 주문 마스터 상태 변경 → 's4' (반품요청)
  void updateOrderStatusCustomer(String ordCd);

  // 5. 반품 이력 조회
  List<ReturnItemVO> getReturnHistoryByOrdCd(String ordCd);
}