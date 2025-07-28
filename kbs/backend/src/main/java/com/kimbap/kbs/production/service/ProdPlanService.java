package com.kimbap.kbs.production.service;

import java.util.List;

public interface ProdPlanService {

  List<ProdPlanVO> getAllPlans();
  void savePlanWithDetails(ProdPlanFullVO fullVO);  // insert or update
  void deletePlan(String produPlanCd);

  
  List<ProdPlanVO> getPlansByCondition(ProdPlanVO condition);     // 생산계획 조건 검색
  List<ProdPlanDetailVO> getDetailsByPlanCd(String produPlanCd);  // 생산계획상세 조회
  List<ProdVO> getAllProducts();                               // 제품기준정보 ALL 검색
}
