package com.kimbap.kbs.production.service;

import java.util.List;

public interface ProdRequestService {

  List<ProdRequestVO> getRequestByCondition(ProdRequestVO condition);     // 생산요청 조건 검색
  List<ProdRequestDetailVO> getDetailsByReqCd(String produReqCd);         // 생산요청상세 조회
  void saveProdPeq(ProdRequestFullVO fullVO);                             // 생산요청 및 상세 저장
  void deleteProdReq(String produReqCd);                                  // 생산요청과 관련 상세 삭제
}
