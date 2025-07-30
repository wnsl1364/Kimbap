package com.kimbap.kbs.production.service;

import java.util.List;

public interface ProdRequestService {

  List<ProdRequestVO> getRequestByCondition(ProdRequestVO condition);     // 생산요청 조건 검색
  List<ProdRequestDetailVO> getDetailsByReqCd(String produReqCd);         // 생산요청상세 조회
}
