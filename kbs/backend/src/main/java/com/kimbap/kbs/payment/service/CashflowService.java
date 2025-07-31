package com.kimbap.kbs.payment.service;

import java.util.List;
import java.util.Map;


public interface CashflowService {
    List<CashflowVO> getCashflowList(Map<String, Object> params); // 입출금 내역 조회
    void insertCf(CashflowVO cf); // 입출금 내역 등록
    void updateCf(CashflowVO cf); // 입출금 내역 수정
    Map<String, Object> getCfDetail(String statementCd); // 입출금 내역 단건 조회
}
