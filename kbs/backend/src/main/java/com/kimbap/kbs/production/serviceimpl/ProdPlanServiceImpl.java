package com.kimbap.kbs.production.serviceimpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kimbap.kbs.production.mapper.ProdPlanMapper;
import com.kimbap.kbs.production.service.BomDetailVO;
import com.kimbap.kbs.production.service.MrpDetailVO;
import com.kimbap.kbs.production.service.MrpVO;
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

    // MRP ================================================================================
    @Transactional
    @Override
    public void runMrpByProdPlan(String produPlanCd) {
        String mrpCd = mapper.getNewMrpCd(); // MRP 코드 생성

        // MRP 마스터 저장
        MrpVO mrp = new MrpVO();
        mrp.setMrpCd(mrpCd);
        mrp.setProduPlanCd(produPlanCd);
        mrp.setPlanGeneDt(LocalDate.now());
        mrp.setProduStartDt(LocalDate.now());
        mrp.setNote("자동생성");
        mrp.setRegi("시스템");
        mapper.insertMrp(mrp);

        // 생산계획 상세 조회
        List<ProdPlanDetailVO> planDetails = mapper.selectDetailsByPlanCd(produPlanCd);

        for (ProdPlanDetailVO detail : planDetails) {
            List<BomDetailVO> bomList = mapper.selectBomMaterials(detail.getPcode(), detail.getProdVerCd());

            for (BomDetailVO bom : bomList) {
                BigDecimal requiredQty = bom.getNeedQty().multiply(new BigDecimal(detail.getPlanQty()));
                BigDecimal stockQty = mapper.selectTotalStockByMate(bom.getMcode(), bom.getMateVerCd());
                BigDecimal lackQty = requiredQty.subtract(stockQty).max(BigDecimal.ZERO);

                MrpDetailVO mrpD = new MrpDetailVO();
                mrpD.setMrpCd(mrpCd);
                mrpD.setMcode(bom.getMcode());
                mrpD.setMateVerCd(bom.getMateVerCd());
                mrpD.setRequiredQty(lackQty);
                mrpD.setUnit(bom.getUnit());
                mrpD.setField("생산계획");

                mapper.insertMrpDetail(mrpD);
            }
        }
    }

}