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
    List<MaterialsVO> getPurcOrderDetailListForApproval();

    /**
     * 🔄 발주 상태 업데이트 (승인/반려용)
     *
     * @param statusData 상태 변경 데이터 (purcDCd, purcDStatus 포함)
     */
    void updatePurchaseOrderStatus(MaterialsVO statusData);

    /**
     * 📋 승인 대기 발주 목록 조회
     *
     * @param criteria 검색 조건
     * @return 승인 대기 발주 목록
     */
    List<MaterialsVO> getPendingApprovalOrders(SearchCriteria criteria);

    /**
     * 📊 발주 통계 조회
     *
     * @param criteria 기간 조건
     * @return 발주 통계 데이터
     */
    Map<String, Object> getPurchaseOrderStatistics(SearchCriteria criteria);

    /**
     * 🔔 발주 상태 변경 알림 처리
     *
     * @param purcDCd 발주상세코드
     * @param newStatus 새로운 상태
     * @param approver 승인자
     */
    void sendStatusChangeNotification(String purcDCd, String newStatus, String approver);

    List<PurchaseOrderViewVO> getPurchaseOrdersForView(SearchCriteria criteria);

    List<PurchaseOrderViewVO> getSupplierMateRelList(SearchCriteria criteria);

    MaterialsVO getPurcOrderDetailByCode(String purcDCd);

    /**
     * 🔢 mate_inbo 테이블에서 가장 마지막 입고코드 조회 MATI-202508-XXXX 형태에서 마지막 번호를 찾기 위함
     *
     * @param yearMonth YYYYMM 형태 (예: 202508)
     * @return 해당 년월의 마지막 입고코드
     */
    String getLastMateInboCode(String yearMonth);

    /**
     * 🔄 발주상세의 curr_qty 업데이트 (누적)
     *
     * @param updateData purcDCd와 새로운 currQty 포함
     */
    void updatePurchaseOrderCurrQty(MaterialsVO updateData);

    /**
     * 🎯 DB에서 원자적으로 다음 입고코드 생성 (동시성 안전)
     *
     * @param yearMonth YYYYMM 형태
     * @return 다음 입고코드 (MATI-YYYYMM-XXXX)
     */
    String generateNextMateInboCode(String yearMonth);

    void updatePurchaseOrderCurrQtyAndStatus(MaterialsVO updateData);

    // 자재 입출고 목록 조회
    List<MaterialsVO> getMaterialFlowList(MaterialsVO search);

    List<MaterialsVO> getTodayMaterialFlowList();

    List<MaterialsVO> getMaterialStockStatus(MaterialsVO searchParams);

    /**
     * 🔍 LOT별 재고 조회
     *
     * @param mcode 자재코드
     * @return LOT별 재고 목록
     */
    public List<MaterialsVO> getMaterialLotStock(String mcode);
}
