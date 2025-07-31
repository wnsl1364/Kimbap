package com.kimbap.kbs.production.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kimbap.kbs.production.service.ProdRequestDetailVO;
import com.kimbap.kbs.production.service.ProdRequestFullVO;
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
  // 생산요청코드별 생산요청상세 조회
  @GetMapping("/{produReqCd}")
  public List<ProdRequestDetailVO> getDetailsByReqCd(@PathVariable String produReqCd) {
    return service.getDetailsByReqCd(produReqCd);
  }
  // 생산요청 및 상세 저장    
  @PostMapping("/requestSave")
  public void saveProdPeq(@RequestBody ProdRequestFullVO fullVO) {
    service.saveProdPeq(fullVO);
  }
  // 생산요청과 관련 상세 삭제
  @DeleteMapping("/{produReqCd}")
  public ResponseEntity<Void> deleteProdPlan(@PathVariable String produReqCd) {
      service.deleteProdReq(produReqCd);
      return ResponseEntity.noContent().build();
  }
}
