package com.kimbap.kbs.production.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kimbap.kbs.production.service.FactoryService;
import com.kimbap.kbs.production.service.FactoryVO;

@RestController
@RequestMapping("/api/prod/factory")
public class FactoryController {
  
  @Autowired
  private FactoryService factoryService;
  
  // 공장 목록 조회 가져오기
  @GetMapping("/list")
  public List<FactoryVO> getFactoryList(){
      return factoryService.getFactoryList();
  }
  
}
