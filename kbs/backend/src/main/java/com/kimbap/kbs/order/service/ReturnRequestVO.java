package com.kimbap.kbs.order.service;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 반품 요청 전체 VO (상위)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReturnRequestVO {

    private String ordCd; // 주문 코드
    private List<ReturnItemVO> returnItems; // 반품 항목들

}