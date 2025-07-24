package com.kimbap.kbs.production.mapper;

import java.util.List;

import com.kimbap.kbs.production.service.ProdPlanDetailVO;
import com.kimbap.kbs.production.service.ProdPlanVO;

public interface ProdPlanMapper {
    // Master
    List<ProdPlanVO> selectProdPlans();
    ProdPlanVO selectProdPlanById(String produPlanCd);
    int insertProdPlan(ProdPlanVO plan);
    int updateProdPlan(ProdPlanVO plan);
    int deleteProdPlan(String produPlanCd);

    // Detail
    List<ProdPlanDetailVO> selectDetailsByPlanCd(String produPlanCd);
    int insertProdPlanDetail(ProdPlanDetailVO detail);
    int updateProdPlanDetail(ProdPlanDetailVO detail);
    int deleteDetailsByPlanCd(String produPlanCd);
}
