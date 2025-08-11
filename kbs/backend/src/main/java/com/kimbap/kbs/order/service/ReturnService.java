package com.kimbap.kbs.order.service;

import java.util.List;
import java.util.Map;

public interface ReturnService {

    // 반품 요청 등록
    void registerReturn(ReturnRequestVO request);

    // 반품 이력조회
    List<ReturnItemVO> getReturnHistoryByOrdCd(String ordCd);

    // 반품 목록 조회
    List<ReturnItemVO> getReturnList(Map<String, Object> params);

    // 반품 승인
    void approveReturn(ReturnItemVO request);

    // 반품 승인(다중)
    void approveReturns(List<ReturnItemVO> requests);

    // 반품 거절
    void rejectReturn(ReturnItemVO request);

    // 반품 취소 (주문상세 기준으로 처리)
    void cancelReturnItems(List<String> ordDCdList);

    // LOT 목록 조회
    List<String> getLotList(String ordDCd);
}