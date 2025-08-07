package com.kimbap.kbs.payment.mapper;

import com.kimbap.kbs.payment.service.UnpaidSettleVO;

public interface UnpaidSettleMapper {
    int updateCashflowStatus(UnpaidSettleVO vo);      // 입금 상태값 정산완료(x2)로 변경
    int deductUnpaidAmount(UnpaidSettleVO vo);     // 거래처 미정산금액 차감
    int updateStatementCpCd(UnpaidSettleVO vo); //
}
