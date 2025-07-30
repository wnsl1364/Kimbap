package com.kimbap.kbs.production.mapper;

import java.util.List;

import com.kimbap.kbs.production.service.ProdPlanDetailVO;
import com.kimbap.kbs.production.service.ProdPlanVO;
import com.kimbap.kbs.production.service.ProdsVO;

public interface ProdPlanMapper {
    // 코드 생성용 함수 호출
    String getNewProdPlanCd(); // 생산계획 PK
    String getNewPpdcode();    // 생산계획상세 PK
    
    // Master
    List<ProdPlanVO> selectProdPlans();                 // 전체 생산계획 조회
    int insertProdPlan(ProdPlanVO plan);                // 생산계획 등록

    // Detail
    int insertProdPlanDetail(ProdPlanDetailVO detail);  // 생산계획상세 등록

    List<ProdPlanVO> selectProdPlansByCondition(ProdPlanVO condition);   // 생산계획 조건 검색
    List<ProdPlanDetailVO> selectDetailsByPlanCd(String produPlanCd);    // 생산계획상세 검색
    List<ProdsVO> selectAllProducts();                                   // 제품기준정보 ALL 검색
    void updateProdPlan(ProdPlanVO plan);                                // 생산계획 업데이트
    void deleteProdPlanDetail(String ppdcode);                           // 생산계획 상세 삭제
    void updateProdPlanDetail(ProdPlanDetailVO detail);                  // 생산계획 상세 업데이트
}
