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
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

        // 주문 상태 코드 초기값 설정
        orderVO.setOrdStatusInternal("a1");   // 내부 요청 상태
        orderVO.setOrdStatusCustomer("s1");   // 고객 접수 대기 상태

        // 2. 주문 마스터 등록
        orderMapper.insertOrderMaster(orderVO);

        // 3. 주문 상세 등록 (반복)
        if (orderVO.getOrderDetails() != null) {
            for (OrderDetailVO detail : orderVO.getOrderDetails()) {
                detail.setOrdCd(newOrderCode); // 외래키 세팅
                detail.setIsUsed("f1");

                detail.setDeliAvailDt(null);

                // 시퀀스로 주문상세코드 생성
                String year = new SimpleDateFormat("yyyy").format(new Date());
                int seq = orderMapper.getNextOrderDetailSeq();
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
        // 0. 기존 주문 상태 조회 → 상태 null로 들어올 경우 기존 상태 유지
        OrderVO existingOrder = orderMapper.selectOrder(orderVO.getOrdCd());
        if (existingOrder == null) {
            throw new IllegalArgumentException("존재하지 않는 주문코드입니다: " + orderVO.getOrdCd());
        }

        if (orderVO.getOrdStatusInternal() == null) {
            orderVO.setOrdStatusInternal(existingOrder.getOrdStatusInternal());
        }
        if (orderVO.getOrdStatusCustomer() == null) {
            orderVO.setOrdStatusCustomer(existingOrder.getOrdStatusCustomer());
        }
        if (orderVO.getOrdDt() == null) {
            orderVO.setOrdDt(existingOrder.getOrdDt());
        }
        if (orderVO.getDeliReqDt() == null) {
            orderVO.setDeliReqDt(existingOrder.getDeliReqDt());
        }
        if (orderVO.getExPayDt() == null) {
            orderVO.setExPayDt(existingOrder.getExPayDt());
        }
        if (orderVO.getDeliAdd() == null) {
            orderVO.setDeliAdd(existingOrder.getDeliAdd());
        }
        if (orderVO.getNote() == null) {
            orderVO.setNote(existingOrder.getNote());
        }
        if (orderVO.getOrdTotalAmount() == null) {
            orderVO.setOrdTotalAmount(existingOrder.getOrdTotalAmount());
        }

        // 승인 상태일 경우 납기가능일자 필수 입력 검증
        if ("a2".equals(orderVO.getOrdStatusInternal())) {
            List<OrderDetailVO> details = orderVO.getOrderDetails();
            if (details != null) {
                for (OrderDetailVO detail : details) {
                    if (detail.getDeliAvailDt() == null) {
                        throw new IllegalArgumentException("승인 처리 시 납기가능일자가 없는 제품이 있습니다.");
                    }
                }
            }
        }

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

                if (detail.getOrdDStatus() != null) {
                    log.info("💥 상태 강제 업데이트 실행 → 상세코드: {}, 상태: {}", detail.getOrdDCd(), detail.getOrdDStatus());
                    orderMapper.updateOrderStatus(detail);
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
