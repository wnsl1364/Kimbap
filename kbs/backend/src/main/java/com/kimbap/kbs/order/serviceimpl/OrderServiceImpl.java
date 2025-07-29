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

                // 시퀀스로 주문상세코드 생성
                int seq = orderMapper.getNextOrderDetailSeq();
                String year = new SimpleDateFormat("yyyy").format(new Date());
                String newOrdDCd = "ORDD-" + year + "-" + String.format("%06d", seq);
                detail.setOrdDCd(newOrdDCd);

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
        List<OrderDetailVO> details = orderMapper.selectOrderDetail(ordCd);
        order.setOrderDetails(details);
        return order;
    }

    @Override
    @Transactional
    public void updateOrder(OrderVO orderVO) {
        // 1. 주문 마스터 수정
        orderMapper.updateOrderMaster(orderVO);

        // 2. 주문 상세 처리
        if (orderVO.getOrderDetails() != null) {
            for (OrderDetailVO detail : orderVO.getOrderDetails()) {
                detail.setOrdCd(orderVO.getOrdCd());
                detail.setIsUsed("f1");

                if (detail.getOrdDCd() == null || detail.getOrdDCd().isEmpty()) {
                    // 신규 제품 → 시퀀스로 코드 생성 후 INSERT
                    int seq = orderMapper.getNextOrderDetailSeq(); // 시퀀스
                    String year = new SimpleDateFormat("yyyy").format(new Date());
                    String newOrdDCd = "ORDD-" + year + "-" + String.format("%06d", seq);
                    detail.setOrdDCd(newOrdDCd);

                    orderMapper.insertOrderDetail(detail);
                } else {
                    // 기존 제품 → UPDATE
                    orderMapper.updateOrderDetail(detail);
                }
            }
        }

        // 3. 삭제 대상 상세가 있다면 삭제 처리 (프론트에서 넘겨줄 경우)
        if (orderVO.getDeletedOrdDCdList() != null) {
            for (String ordDCd : orderVO.getDeletedOrdDCdList()) {
                orderMapper.deleteOrderDetailByOrdDCd(ordDCd);
            }
        }
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
