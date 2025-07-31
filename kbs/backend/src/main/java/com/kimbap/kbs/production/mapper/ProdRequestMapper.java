package com.kimbap.kbs.production.mapper;

import java.util.List;

import com.kimbap.kbs.production.service.ProdRequestDetailVO;
import com.kimbap.kbs.production.service.ProdRequestVO;


public interface ProdRequestMapper {
    // 코드 생성용 함수 호출
    String getNewProduReqCd();     // 생산요청 PK
    String getNewProduProdCd();    // 생산요청상세 PK

    // 검색 기능 =========================================
    List<ProdRequestVO> selectProdRequestByCondition(ProdRequestVO condition);   // 생산요청 조건 검색
    List<ProdRequestDetailVO> selectDetailsByProduReqCd(String produReqCd);      // 생산요청상세 조회

    // 저장 기능 =========================================
    int insertProductionReq(ProdRequestVO request);                              // 생산요청 등록
    int insertProdReqDetail(ProdRequestDetailVO reqDetails);                     // 생산요청상세 등록
    void deleteProdReqDetail(String produProdCd);                                // 생산요청 상세 삭제(수정 과정에서 삭제)
    void updateProductionReq(ProdRequestVO request);                             // 생산요청 업데이트
    void updateProdReqDetail(ProdRequestDetailVO detail);                        // 생산요청 상세 업데이트

    // 삭제 기능 ========================================
    void deleteProdReqDetailByReqCd(String produReqCd);                         // 생산계획코드 조건으로 상세 삭제
    void deleteProductionReq(String produReqCd);                                 // 생산계획 삭제
}
