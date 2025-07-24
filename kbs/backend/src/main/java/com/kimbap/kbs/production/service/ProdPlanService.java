package com.kimbap.kbs.production.service;

import java.util.List;

public interface ProdPlanService {

  List<ProdPlanVO> getAllPlans();
  ProdPlanFullVO getPlanWithDetails(String produPlanCd);
  void savePlanWithDetails(ProdPlanFullVO fullVO);  // insert or update
  void deletePlan(String produPlanCd);
}
