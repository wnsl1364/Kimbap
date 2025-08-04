package com.kimbap.kbs.payment.service;

public interface UnpaidService {
    void settleUnpaid(UnpaidSettleVO vo);
}