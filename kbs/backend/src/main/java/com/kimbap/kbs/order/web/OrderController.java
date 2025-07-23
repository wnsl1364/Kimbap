package com.kimbap.kbs.order.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kimbap.kbs.order.service.OrderService;
import com.kimbap.kbs.order.service.OrderVO;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/order")
public class OrderController {
  @Autowired
  private OrderService orderService;

  // 주문 목록 조회
  @GetMapping("/list")
  public List<OrderVO> getOrderList() {
      return orderService.getOrderList();
  }
  // 주문 등록
  @PostMapping("/register")
  public void registerOrder(@RequestBody OrderVO order) {
      orderService.insertOrder(order);
  }
}
