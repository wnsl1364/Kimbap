package com.kimbap.kbs.production.serviceimpl;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
        ProdPlanVO plan = fullVO.getPlan();
        List<ProdPlanDetailVO> details = fullVO.getPlanDetails();

        boolean isNew = (plan.getProduPlanCd() == null || plan.getProduPlanCd().isEmpty());
        String produPlanCd = isNew ? mapper.getNewProdPlanCd() : plan.getProduPlanCd();
        plan.setProduPlanCd(produPlanCd);

        if (isNew) {
            mapper.insertProdPlan(plan);
        } else {
            mapper.updateProdPlan(plan);
        }

        // 기존 상세 목록 가져오기
        List<ProdPlanDetailVO> existingDetails = mapper.selectDetailsByPlanCd(produPlanCd);
        Set<String> incomingPpdcodes = details.stream()
                                            .map(ProdPlanDetailVO::getPpdcode)
                                            .filter(Objects::nonNull)
                                            .collect(Collectors.toSet());

        // 삭제 대상만 삭제
        for (ProdPlanDetailVO exist : existingDetails) {
            if (!incomingPpdcodes.contains(exist.getPpdcode())) {
                mapper.deleteProdPlanDetail(exist.getPpdcode());
            }
        }

        // 신규 또는 수정 분기 처리
        for (ProdPlanDetailVO detail : details) {
            detail.setProduPlanCd(produPlanCd);

            if (detail.getPpdcode() == null || detail.getPpdcode().isEmpty()) {
                detail.setPpdcode(mapper.getNewPpdcode());
                mapper.insertProdPlanDetail(detail);
            } else {
                mapper.updateProdPlanDetail(detail);
            }
        }
    }
    // 생산계획과 관련 상세 삭제
    @Transactional
    @Override
    public void deleteProdPlan(String produPlanCd) {
        mapper.deleteProdPlanDetailByPlanCd(produPlanCd);
        mapper.deleteProdPlan(produPlanCd);
    }
}