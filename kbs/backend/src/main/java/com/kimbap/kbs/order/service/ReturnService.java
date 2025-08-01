package com.kimbap.kbs.order.service;

public interface ReturnService {

    // 반품 요청 등록
    void registerReturn(ReturnRequestVO request);
}