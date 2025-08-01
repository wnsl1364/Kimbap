package com.kimbap.kbs.production.service;

import java.util.List;

public interface ProdPlanService {

  List<ProdPlanVO> getAllPlans();
  
  List<ProdPlanVO> getPlansByCondition(ProdPlanVO condition);     // 생산계획 조건 검색
  List<ProdPlanDetailVO> getDetailsByPlanCd(String produPlanCd);  // 생산계획상세 조회
  List<ProdsVO> getAllProducts();                                 // 제품기준정보 ALL 검색
  void saveProdPlan(ProdPlanFullVO fullVO);                       // 생산계획 및 상세 저장
  void deleteProdPlan(String produPlanCd);                        // 생산계획과 관련 상세 삭제
}
