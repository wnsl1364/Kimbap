package com.kimbap.kbs.production.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kimbap.kbs.production.service.ProdRequestDetailVO;
import com.kimbap.kbs.production.service.ProdRequestService;
import com.kimbap.kbs.production.service.ProdRequestVO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/prod/request")
@RequiredArgsConstructor
public class ProdRequestController {
  private final ProdRequestService service;

  // 생산요청 조건 검색
  @PostMapping("/search")
  public List<ProdRequestVO> searchRequest(@RequestBody ProdRequestVO condition) {
      return service.getRequestByCondition(condition);
  }
  // 생산계획코드별 생산계획상세 조회
  @GetMapping("/{produReqCd}")
  public List<ProdRequestDetailVO> getDetailsByReqCd(@PathVariable String produReqCd) {
      return service.getDetailsByReqCd(produReqCd);
  }
}
