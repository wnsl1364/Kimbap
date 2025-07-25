package com.kimbap.kbs.order.serviceimpl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kimbap.kbs.order.mapper.OrderMapper;
import com.kimbap.kbs.order.service.OrderDetailVO;
import com.kimbap.kbs.order.service.OrderService;
import com.kimbap.kbs.order.service.OrderVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl  implements OrderService {

    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public void registerOrder(OrderVO orderVO) {
        // 1. 주문코드 생성
        String newOrderCode = generateOrderCode();
        orderVO.setOrdCd(newOrderCode);

        // 2. 주문 마스터 등록
        orderMapper.insertOrderMaster(orderVO);

        // 3. 주문 상세 등록 (반복)
        if (orderVO.getOrderDetails() != null) {
            for (OrderDetailVO detail : orderVO.getOrderDetails()) {
                detail.setOrdCd(newOrderCode); // 외래키 세팅
                String ordDCd = orderMapper.getGeneratedOrderDetailCode(); // PK 코드 생성
                detail.setOrdDCd(ordDCd); // DB 함수로 생성
                orderMapper.insertOrderDetail(detail);
            }
        }
    }

    @Override
    public List<OrderVO> getOrderList() {
        return orderMapper.getOrderList();
    }

    // 주문코드 생성 메서드 (요구사항 형식 맞춤: ORD-20250001)
    private String generateOrderCode() {
        String year = new SimpleDateFormat("yyyy").format(new Date());
        String prefix = "ORD-" + year;

        String latest = orderMapper.getLatestOrderCode();

        int nextNumber = 1;
        if (latest != null && latest.startsWith(prefix)) {
            String lastSeq = latest.substring(prefix.length()); // '0001' 이런 숫자 부분
            nextNumber = Integer.parseInt(lastSeq) + 1;
        }

        return prefix + String.format("%04d", nextNumber); // 4자리 형식
    }  
}
