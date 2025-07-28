package com.kimbap.kbs.production.mapper;

import java.util.List;

import com.kimbap.kbs.production.service.ProdPlanDetailVO;
import com.kimbap.kbs.production.service.ProdPlanVO;
import com.kimbap.kbs.production.service.ProdsVO;

public interface ProdPlanMapper {
    // Master
    List<ProdPlanVO> selectProdPlans();
    int insertProdPlan(ProdPlanVO plan);
    int updateProdPlan(ProdPlanVO plan);
    int deleteProdPlan(String produPlanCd);

    // Detail
    int insertProdPlanDetail(ProdPlanDetailVO detail);
    int updateProdPlanDetail(ProdPlanDetailVO detail);
    int deleteDetailsByPlanCd(String produPlanCd);
    

    List<ProdPlanVO> selectProdPlansByCondition(ProdPlanVO condition);   // 생산계획 조건 검색
    List<ProdPlanDetailVO> selectDetailsByPlanCd(String produPlanCd);    // 생산계획상세 검색
    List<ProdsVO> selectAllProducts();                                    // 제품기준정보 ALL 검색
}
