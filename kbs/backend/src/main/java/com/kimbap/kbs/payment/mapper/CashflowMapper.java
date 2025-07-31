package com.kimbap.kbs.payment.mapper;

import java.util.List;
import java.util.Map;

import com.kimbap.kbs.payment.service.CashflowVO;

public interface CashflowMapper {
    List<CashflowVO> getCashflowList(Map<String, Object> params); // 입출금 내역 목록 조회
    void insterCf(CashflowVO cf);    // 입출금 등록
    void updateCf(CashflowVO cf);    // 입출금 수정
    CashflowVO getCfDetail(String statementCd); // 입출금 내역 단건 조회
    int existsCfcode(String statementCd); // 입출금내역코드 존재 여부확인
    int getNextInCashflowCode(); // 시퀀스
    int getNextOutCashflowCode(); // 시퀀스
}
