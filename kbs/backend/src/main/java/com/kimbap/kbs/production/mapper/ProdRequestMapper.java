package com.kimbap.kbs.production.mapper;

import java.util.List;

import com.kimbap.kbs.production.service.ProdRequestDetailVO;
import com.kimbap.kbs.production.service.ProdRequestVO;


public interface ProdRequestMapper {
    // 검색 기능 =========================================
    List<ProdRequestVO> selectProdRequestByCondition(ProdRequestVO condition);   // 생산요청 조건 검색
    List<ProdRequestDetailVO> selectDetailsByProduReqCd(String produReqCd);             // 생산요청상세 조회
}
