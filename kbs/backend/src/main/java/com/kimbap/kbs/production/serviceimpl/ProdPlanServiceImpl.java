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
import com.kimbap.kbs.production.service.ProdsVO;

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

    // 생산계획 조건 검색
    @Override
        public List<ProdPlanVO> getPlansByCondition(ProdPlanVO condition) {
        return mapper.selectProdPlansByCondition(condition);
    }
    // 생산계획코드별 생산계획상세 조회
    @Override
    public List<ProdPlanDetailVO> getDetailsByPlanCd(String produPlanCd) {
        return mapper.selectDetailsByPlanCd(produPlanCd);
    }
    // 제품기준정보 ALL 검색
    @Override
    public List<ProdsVO> getAllProducts() {
        return mapper.selectAllProducts();
    }
    // 생산계획 및 상세 저장
    @Override
    @Transactional
    public void saveProdPlan(ProdPlanFullVO fullVO) {
        String produPlanCd = mapper.getNewProdPlanCd(); // 생산계획 번호 생성
        ProdPlanVO plan = fullVO.getPlan();
        plan.setProduPlanCd(produPlanCd);

        mapper.insertProdPlan(plan);
     
        for (ProdPlanDetailVO detail : fullVO.getPlanDetails()) {
            detail.setProduPlanCd(produPlanCd);        // 생산계획(FK) 삽입
            detail.setPpdcode(mapper.getNewPpdcode()); // 상세 목록 삽입
            mapper.insertProdPlanDetail(detail);        
        }
    }
}