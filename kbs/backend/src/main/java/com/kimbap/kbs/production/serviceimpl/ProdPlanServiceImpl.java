package com.kimbap.kbs.production.serviceimpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kimbap.kbs.production.mapper.ProdPlanMapper;
import com.kimbap.kbs.production.service.BomDetailVO;
import com.kimbap.kbs.production.service.MateSupplierVO;
import com.kimbap.kbs.production.service.MaterVO;
import com.kimbap.kbs.production.service.MrpDetailVO;
import com.kimbap.kbs.production.service.MrpPreviewVO;
import com.kimbap.kbs.production.service.MrpVO;
import com.kimbap.kbs.production.service.ProdPlanDetailVO;
import com.kimbap.kbs.production.service.ProdPlanFullVO;
import com.kimbap.kbs.production.service.ProdPlanService;
import com.kimbap.kbs.production.service.ProdPlanVO;
import com.kimbap.kbs.production.service.ProdsVO;
import com.kimbap.kbs.production.service.PurcOrdDetailVO;
import com.kimbap.kbs.production.service.PurcOrdVO;

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

    // 발주서 등록 기능 =========================================================================
    @Transactional
    @Override
    public void createPurchaseOrderFromMrp(String mrpCd) {
        // 1. MRP 상세 목록 조회 (부족한 자재 목록)
        List<MrpDetailVO> mrpDetails = mapper.selectMrpDetailsByMrpCd(mrpCd);
        
        if (mrpDetails.isEmpty()) {
            return; // 발주할 자재가 없으면 종료
        }
        
        // 2. 발주서 마스터 생성
        String purcCd = mapper.getNewPurcCd();
        PurcOrdVO purchaseOrder = new PurcOrdVO();
        purchaseOrder.setPurcCd(purcCd);
        purchaseOrder.setOrdDt(LocalDate.now());
        purchaseOrder.setRegi("시스템"); // 또는 현재 사용자 정보
        purchaseOrder.setPurcStatus("대기"); // 발주 상태 초기값
        purchaseOrder.setOrdTotalAmount(BigDecimal.ZERO); // 일단 0으로 설정, 상세 저장 후 업데이트
        
        mapper.insertPurchaseOrder(purchaseOrder);
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        // 3. 각 부족 자재별로 발주서 상세 생성
        for (MrpDetailVO mrpDetail : mrpDetails) {
            // 3-1. 해당 자재의 최저가 공급업체 조회
            MateSupplierVO bestSupplier = mapper.selectBestSupplierByMaterial(
                mrpDetail.getMcode(), 
                mrpDetail.getMateVerCd()
            );
            
            if (bestSupplier == null) {
                // 공급업체가 없는 경우 로그 남기고 스킵
                System.out.println("공급업체 없음: " + mrpDetail.getMcode());
                continue;
            }
            
            // 3-2. 자재 기본정보 조회 (최소발주단위 확인용)
            MaterVO material = mapper.selectMaterialInfo(
                mrpDetail.getMcode(), 
                mrpDetail.getMateVerCd()
            );
            
            // 3-3. 발주수량 계산 (최소발주단위 고려)
            BigDecimal requiredQty = mrpDetail.getRequiredQty();
            BigDecimal moqty = material.getMoqty() != null ? material.getMoqty() : BigDecimal.ONE;
            
            // 최소발주단위의 배수로 올림 계산
            BigDecimal purcQty = requiredQty;
            if (requiredQty.remainder(moqty).compareTo(BigDecimal.ZERO) > 0) {
                purcQty = requiredQty.divide(moqty, 0, BigDecimal.ROUND_UP).multiply(moqty);
            }
            
            // 3-4. 납기예정일 계산 (리드타임 기준)
            LocalDate exDeliDt = LocalDate.now().plusDays(bestSupplier.getLtime());
            
            // 3-5. 발주서 상세 생성
            PurcOrdDetailVO orderDetail = new PurcOrdDetailVO();
            orderDetail.setPurcDCd(mapper.getNewPurcDCd());
            orderDetail.setPurcCd(purcCd);
            orderDetail.setMcode(mrpDetail.getMcode());
            orderDetail.setMateVerCd(mrpDetail.getMateVerCd());
            orderDetail.setPurcQty(purcQty);
            orderDetail.setUnit(mrpDetail.getUnit());
            orderDetail.setUnitPrice(bestSupplier.getUnitPrice());
            orderDetail.setExDeliDt(exDeliDt);
            orderDetail.setNote("MRP 자동생성 - 소요량: " + requiredQty);
            orderDetail.setPurcDStatus("대기");
            orderDetail.setMateCpCd(bestSupplier.getMateCpCd());
            orderDetail.setCurrQty(BigDecimal.ZERO);
            
            mapper.insertPurchaseOrderDetail(orderDetail);
            
            // 3-6. 총액 계산
            totalAmount = totalAmount.add(purcQty.multiply(bestSupplier.getUnitPrice()));
        }
        
        // 4. 발주서 총액 업데이트
        purchaseOrder.setOrdTotalAmount(totalAmount);
        mapper.updatePurchaseOrderTotalAmount(purcCd, totalAmount);
    }

    @Override
    public MrpPreviewVO getMrpPreview(ProdPlanFullVO fullVO) {
        String virtualMrpCd = "PREVIEW-MRP-" + System.currentTimeMillis();
        String virtualPurcCd = "PREVIEW-PURC-" + System.currentTimeMillis();
        
        MrpPreviewVO preview = new MrpPreviewVO();
        preview.setPreviewMrpCd(virtualMrpCd);
        preview.setPreviewPurcCd(virtualPurcCd);
        
        // 🎯 핵심: 기존 MRP 로직을 그대로 사용하되 DB에 저장하지 않음
        List<MrpDetailVO> mrpDetails = simulateMrpGeneration(fullVO);
        List<PurcOrdDetailVO> purchaseOrderDetails = simulatePurchaseOrderGeneration(mrpDetails, virtualPurcCd);
        
        preview.setMrpDetails(mrpDetails);
        preview.setPurchaseOrderDetails(purchaseOrderDetails);
        
        return preview;
    }
    
    /**
     * 기존 runMrpByProdPlan 로직을 시뮬레이션 (DB 저장 없이)
     */
    private List<MrpDetailVO> simulateMrpGeneration(ProdPlanFullVO fullVO) {
        List<MrpDetailVO> mrpDetails = new ArrayList<>();
        
        // 🔄 기존 MRP 로직과 동일한 처리
        for (ProdPlanDetailVO detail : fullVO.getPlanDetails()) {
            List<BomDetailVO> bomList = mapper.selectBomMaterials(detail.getPcode(), detail.getProdVerCd());
            
            for (BomDetailVO bom : bomList) {
                BigDecimal requiredQty = bom.getNeedQty().multiply(new BigDecimal(detail.getPlanQty()));
                BigDecimal stockQty = mapper.selectTotalStockByMate(bom.getMcode(), bom.getMateVerCd());
                BigDecimal lackQty = requiredQty.subtract(stockQty).max(BigDecimal.ZERO);
                
                if (lackQty.compareTo(BigDecimal.ZERO) > 0) {
                    MrpDetailVO mrpDetail = new MrpDetailVO();
                    // mrpDetail.setMrpCd("PREVIEW"); // 실제 저장하지 않으므로 가상 코드
                    mrpDetail.setMcode(bom.getMcode());
                    mrpDetail.setMateVerCd(bom.getMateVerCd());
                    mrpDetail.setRequiredQty(lackQty);
                    mrpDetail.setUnit(bom.getUnit());
                    mrpDetail.setField("생산계획");
                    
                    mrpDetails.add(mrpDetail);
                }
            }
        }
        
        return mrpDetails;
    }
    
    /**
     * 기존 createPurchaseOrderFromMrp 로직을 시뮬레이션 (DB 저장 없이)
     */
    private List<PurcOrdDetailVO> simulatePurchaseOrderGeneration(List<MrpDetailVO> mrpDetails, String virtualPurcCd) {
        List<PurcOrdDetailVO> purchaseOrderDetails = new ArrayList<>();
        
        // 기존 발주서 생성 로직과 동일한 처리
        for (MrpDetailVO mrpDetail : mrpDetails) {
            MateSupplierVO bestSupplier = mapper.selectBestSupplierByMaterial(
                mrpDetail.getMcode(), 
                mrpDetail.getMateVerCd()
            );
            
            if (bestSupplier == null) continue;
            
            MaterVO material = mapper.selectMaterialInfo(
                mrpDetail.getMcode(), 
                mrpDetail.getMateVerCd()
            );
            
            // 발주수량 계산 (기존 로직과 동일)
            BigDecimal requiredQty = mrpDetail.getRequiredQty();
            BigDecimal moqty = material.getMoqty() != null ? material.getMoqty() : BigDecimal.ONE;
            BigDecimal purcQty = requiredQty;
            if (requiredQty.remainder(moqty).compareTo(BigDecimal.ZERO) > 0) {
                purcQty = requiredQty.divide(moqty, 0, BigDecimal.ROUND_UP).multiply(moqty);
            }
            
            LocalDate exDeliDt = LocalDate.now().plusDays(bestSupplier.getLtime());
            
            // 기존 PurchaseOrderDetailVO 재사용
            PurcOrdDetailVO orderDetail = new PurcOrdDetailVO();
            orderDetail.setPurcCd(virtualPurcCd);
            orderDetail.setMcode(mrpDetail.getMcode());
            orderDetail.setMateVerCd(mrpDetail.getMateVerCd());
            orderDetail.setPurcQty(purcQty);
            orderDetail.setUnit(mrpDetail.getUnit());
            orderDetail.setUnitPrice(bestSupplier.getUnitPrice());
            orderDetail.setExDeliDt(exDeliDt);
            orderDetail.setNote("MRP 자동생성 - 소요량: " + requiredQty);
            orderDetail.setPurcDStatus("대기");
            orderDetail.setMateCpCd(bestSupplier.getMateCpCd());
            orderDetail.setCurrQty(BigDecimal.ZERO);
            
            purchaseOrderDetails.add(orderDetail);
        }
        
        return purchaseOrderDetails;
    }
}