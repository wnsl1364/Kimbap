package com.kimbap.kbs.materials.service;

import java.util.List;
import java.util.Map;
public interface MateService {
    
    // 자재입고 관련 메서드
    void insertMateInbo(MaterialsVO mateInbo);
    List<MaterialsVO> getMateInboList();
    void updateMateInbo(MaterialsVO mateInbo);
    MaterialsVO getMateInboById(String mateInboCd);

    // 발주 관련 메서드
    List<MaterialsVO> getPurcOrdList();
    List<MaterialsVO> getPurchaseOrders(SearchCriteria criteria);
    List<MaterialsVO> getPurcOrderList();
    Map<String, Object> getPurcOrderWithDetails(String purcCd);
    List<MaterialsVO> getMaterialWithSuppliers(SearchCriteria criteria);
    String savePurchaseOrder(Map<String, Object> orderData);
    String generatePurchaseCode();
    List<MaterialsVO> getSuppliersByMaterial(SearchCriteria criteria);
    List<MaterialsVO> getMaterialsBySupplier(SearchCriteria criteria);

    // 자재출고 관련 메서드
    List<MaterialsVO> getMateRelList();
    void insertMateRel(MaterialsVO mateRel);
    
    // 공장목록 조회 메서드
    List<MaterialsVO> getActiveFactoryList();

    // 발주서 승인 관련 메서드
}