package com.kimbap.kbs.payment.serviceimpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kimbap.kbs.payment.mapper.CashflowMapper;
import com.kimbap.kbs.payment.service.CashflowService;
import com.kimbap.kbs.payment.service.CashflowVO;

@Service
public class CashflowServiceImpl implements CashflowService{

    @Autowired
    private CashflowMapper cashflowMapper;

    // 입출금 내역 목록 조회
    @Override
    public List<CashflowVO> getCashflowList(Map<String, Object> params) {
        return cashflowMapper.getCashflowList(params);  // 👈 Mapper로 그대로 전달
    }

    // 입출금 내역 등록
    @Transactional
    @Override
    public void insertCf(CashflowVO cf) {
        String statementCd;
        String transType = cf.getTransType(); // u1: 입금, u2: 출금

        int next;

        if ("u1".equalsIgnoreCase(transType)) {
            next = cashflowMapper.getNextInCashflowCode(); // 입금 시퀀스 사용
            if (next > 999) throw new RuntimeException("IN 코드 범위 초과 (IN-001 ~ IN-999)");
            statementCd = String.format("IN-%03d", next);

        } else if ("u2".equalsIgnoreCase(transType)) {
            next = cashflowMapper.getNextOutCashflowCode(); // 출금 시퀀스 사용
            if (next > 999) throw new RuntimeException("OUT 코드 범위 초과 (OUT-001 ~ OUT-999)");
            statementCd = String.format("OUT-%03d", next);

        } else {
            throw new RuntimeException("알 수 없는 거래유형: " + transType);
        }

        System.out.println("생성된 statementCd : " + statementCd);

        if (cashflowMapper.existsCfcode(statementCd) > 0) {
            throw new RuntimeException("이미 존재하는 입출금 내역 코드: " + statementCd);
        }

        cf.setStatementCd(statementCd);
        if (cf.getRegi() == null || cf.getRegi().isEmpty()) {
            cf.setRegi("박회계");
        }

        cashflowMapper.insterCf(cf);
        System.out.println("등록되는 VO :" + cf);
    }

    // 입출금 내역 수정
    @Transactional
    @Override
    public void updateCf(CashflowVO cf) {
        if (cashflowMapper.existsCfcode(cf.getStatementCd()) == 0) {
            throw new IllegalArgumentException("존재하지 않는 입출금내역 코드입니다: " +cf.getStatementCd());
        }

        // 수정자 기본값 세팅
        if (cf.getModi() == null || cf.getModi().isEmpty()) {
            cf.setModi("박회계");
        }

        cashflowMapper.updateCf(cf);
    }

    // 입출금 내역 단건 조회
    @Override
    public Map<String, Object> getCfDetail(String statementCd) {
        Map<String,Object> result = new HashMap<>();

        CashflowVO cashflow = cashflowMapper.getCfDetail(statementCd);

        result.put("cashflow", cashflow);

        return result;
    }
    
}
