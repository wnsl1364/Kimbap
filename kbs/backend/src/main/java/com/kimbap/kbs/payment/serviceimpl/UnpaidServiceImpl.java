package com.kimbap.kbs.payment.serviceimpl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.kimbap.kbs.payment.mapper.UnpaidSettleMapper;
import com.kimbap.kbs.payment.service.UnpaidService;
import com.kimbap.kbs.payment.service.UnpaidSettleVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UnpaidServiceImpl implements UnpaidService {

    private final UnpaidSettleMapper unpaidSettleMapper;

    @Transactional
    @Override
    public void settleUnpaid(UnpaidSettleVO vo) {
        // 1. 입금내역 상태값 정산완료로 업데이트
        int updated1 = unpaidSettleMapper.updateCashflowStatus(vo);
        if (updated1 == 0) {
            throw new RuntimeException("입금내역 상태 업데이트 실패: " + vo.getStatementCd());
        }

        // 2. 거래처 미정산금액 차감
        int updated2 = unpaidSettleMapper.deductUnpaidAmount(vo);
        if (updated2 == 0) {
            throw new RuntimeException("거래처 미정산금액 차감 실패: " + vo.getCpCd());
        }

        // 3. 입금내역에 거래처코드 저장
        int  updated3 = unpaidSettleMapper.updateStatementCpCd(vo);
        if  (updated3 == 0) {
            throw new RuntimeException("거래처 코드 저장 실패: " + vo.getCpCd());
        }
    }
}