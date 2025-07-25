package com.kimbap.kbs.production.serviceimpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kimbap.kbs.production.mapper.ProdPlanMapper;
import com.kimbap.kbs.production.service.ProdPlanDetailVO;
import com.kimbap.kbs.production.service.ProdPlanFullVO;
import com.kimbap.kbs.production.service.ProdPlanService;
import com.kimbap.kbs.production.service.ProdPlanVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProdPlanServiceImpl implements ProdPlanService {

    @Autowired
    private final ProdPlanMapper mapper;

    @Override
    public List<ProdPlanVO> getAllPlans() {
        return mapper.selectProdPlans();
    }

    @Override
    public ProdPlanFullVO getPlanWithDetails(String produPlanCd) {
        ProdPlanFullVO fullVO = new ProdPlanFullVO();
        fullVO.setPlan(mapper.selectProdPlanById(produPlanCd));
        fullVO.setPlanDetails(mapper.selectDetailsByPlanCd(produPlanCd));
        return fullVO;
    }

    @Override
    @Transactional
    public void savePlanWithDetails(ProdPlanFullVO fullVO) {
        if (mapper.selectProdPlanById(fullVO.getPlan().getProduPlanCd()) == null) {
            mapper.insertProdPlan(fullVO.getPlan());
        } else {
            mapper.updateProdPlan(fullVO.getPlan());
            mapper.deleteDetailsByPlanCd(fullVO.getPlan().getProduPlanCd());  // 덮어쓰기
        }
        for (ProdPlanDetailVO detail : fullVO.getPlanDetails()) {
            mapper.insertProdPlanDetail(detail);
        }
    }

    @Override
    @Transactional
    public void deletePlan(String produPlanCd) {
        mapper.deleteDetailsByPlanCd(produPlanCd);
        mapper.deleteProdPlan(produPlanCd);
    }

    // 생산계획 조건 검색
    @Override
        public List<ProdPlanVO> getPlansByCondition(ProdPlanVO condition) {
        return mapper.selectProdPlansByCondition(condition);
    }
}