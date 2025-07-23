package com.kimbap.kbs.order.serviceimpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kimbap.kbs.order.mapper.OrderMapper;
import com.kimbap.kbs.order.service.OrderService;
import com.kimbap.kbs.order.service.OrderVO;

@Service
public class OrderServiceImpl  implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public List<OrderVO> getOrderList() {
        return orderMapper.getOrderList();
    }

    @Transactional
    @Override
    public void insertOrder(OrderVO order) {
        // 주문 등록 로직
        orderMapper.insertOrder(order);
    }
  
}
