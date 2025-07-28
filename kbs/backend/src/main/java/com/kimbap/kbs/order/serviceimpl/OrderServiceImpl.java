package com.kimbap.kbs.order.serviceimpl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

        // 주문 활성화
        orderVO.setIsUsed("f1");

        // 2. 주문 마스터 등록
        orderMapper.insertOrderMaster(orderVO);

        // 3. 주문 상세 등록 (반복)
        if (orderVO.getOrderDetails() != null) {
            for (OrderDetailVO detail : orderVO.getOrderDetails()) {
                detail.setOrdCd(newOrderCode); // 외래키 세팅
                detail.setIsUsed("f1");
                String ordDCd = orderMapper.getGeneratedOrderDetailCode(); // PK 코드 생성
                detail.setOrdDCd(ordDCd); // DB 함수로 생성
                orderMapper.insertOrderDetail(detail);
            }
        }
    }

    @Override
    public List<OrderVO> getOrderList(Map<String, Object> params) {
        return orderMapper.getOrderList(params);
    }

    @Override
    public void deactivateOrder(String ordCd) {
        orderMapper.deactivateOrder(ordCd);
    }

    @Override
    public OrderVO getOrderWithDetails(String ordCd) {
        OrderVO order = orderMapper.selectOrder(ordCd);
        List<OrderDetailVO> details = orderMapper.selectOrderDetails(ordCd);
        order.setOrderDetails(details);
        return order;
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
