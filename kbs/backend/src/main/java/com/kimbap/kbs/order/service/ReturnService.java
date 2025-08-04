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
    void approveReturn(List<String> prodReturnCds);

    // 반품 반려
    void rejectReturn(List<String> prodReturnCds);
}