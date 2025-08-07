package com.kimbap.kbs.materials.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.kimbap.kbs.materials.service.MaterialsVO;
import com.kimbap.kbs.materials.service.PurchaseOrderViewVO;
import com.kimbap.kbs.materials.service.SearchCriteria;

public interface MateMapper {

    // ========== 기존 메서드들 ==========
    void insertPurcOrd(MaterialsVO purcOrd);

    void updatePurcOrd(MaterialsVO purcOrd);

    List<MaterialsVO> getPurcOrdList();

    List<MaterialsVO> getPurcOrdList(SearchCriteria criteria);

    void insertMateInbo(MaterialsVO mateInbo);

    List<MaterialsVO> getMateInboList();

    void updateMateInbo(MaterialsVO mateInbo);

    MaterialsVO getMateInboById(String mateInboCd);

    List<MaterialsVO> getMateRelList();

    void insertMateRel(MaterialsVO mateRel);

    // ========== 새로 추가된 발주서 관련 메서드 ==========
    /**
     * 발주서 목록 조회 (모달용)
     */
    List<MaterialsVO> getPurcOrderList();

    /**
     * 발주서 상세 조회 (헤더 + 상세)
     */
    List<MaterialsVO> getPurcOrderWithDetails(String purcCd);

    /**
     * 자재-거래처 연결 조회 (검색용)
     */
    List<MaterialsVO> getMaterialWithSuppliers(SearchCriteria criteria);

    /**
     * 발주서 헤더 등록
     */
    void insertPurcOrder(MaterialsVO purcOrder);

    /**
     * 발주서 상세 등록
     */
    void insertPurcOrderDetail(MaterialsVO purcOrderDetail);

    /**
     * 발주서 헤더 수정
     */
    void updatePurcOrder(MaterialsVO purcOrder);

    int countLotsByPattern(@Param("lotPattern") String lotPattern);

    String getMaterialType(@Param("mcode") String mcode);

    List<MaterialsVO> getActiveFactoryList();

    String getLastPurcCode();

    void deletePurcOrderDetails(String purcCd);

    void updatePurcOrderDetail(MaterialsVO purcOrderDetail);

    String getLastPurcDetailCode();

    List<MaterialsVO> getSuppliersByMaterial(SearchCriteria criteria);

    List<MaterialsVO> getMaterialsBySupplier(SearchCriteria criteria);

    List<MaterialsVO> findMateSupplier(SearchCriteria criteria);

    MaterialsVO getMateSupplierByKey(SearchCriteria criteria);

    String getMateCpCd(@Param("mcode") String mcode,
            @Param("mateVerCd") String mateVerCd,
            @Param("cpCd") String cpCd);

    List<MaterialsVO> getPurcOrderDetailListForApproval();

    /**
     * 🔄 발주 상세 상태 업데이트 (승인/반려용)
     */
    void updatePurcOrderDetailStatus(MaterialsVO statusData);

    /**
     * 🔄 발주 헤더 상태 업데이트
     */
    void updatePurcOrderHeaderStatus(MaterialsVO headerData);

    /**
     * 📋 승인 대기 발주 목록 조회 (상세 정보 포함)
     */
    List<MaterialsVO> getPendingApprovalOrdersDetailed(SearchCriteria criteria);

    /**
     * 📊 발주 상태별 통계 조회
     */
    List<Map<String, Object>> getPurchaseOrderStatusStatistics(SearchCriteria criteria);

    /**
     * 📊 월별 발주 통계 조회
     */
    List<Map<String, Object>> getMonthlyPurchaseStatistics(SearchCriteria criteria);

    /**
     * 📊 공급업체별 발주 통계 조회
     */
    List<Map<String, Object>> getSupplierPurchaseStatistics(SearchCriteria criteria);

    /**
     * 🔍 발주 상세 정보 조회 (승인 이력 포함)
     */
    MaterialsVO getPurchaseOrderDetailWithHistory(String purcDCd);

    List<PurchaseOrderViewVO> getPurchaseOrdersForView(SearchCriteria criteria);

    List<PurchaseOrderViewVO> getSupplierMateRelList(SearchCriteria criteria);

    MaterialsVO getPurcOrderDetailByCode(String purcDCd);

    /**
     * 🔢 mate_inbo 테이블에서 가장 마지막 입고코드 조회
     * MATI-202508-XXXX 형태에서 마지막 번호를 찾기 위함
     * 
     * @param yearMonth YYYYMM 형태 (예: 202508)
     * @return 해당 년월의 마지막 입고코드
     */
    String getLastMateInboCode(@Param("yearMonth") String yearMonth);

    /**
     * 🔄 발주상세의 curr_qty 업데이트 (누적)
     * 
     * @param updateData purcDCd와 새로운 currQty 포함
     */
    void updatePurchaseOrderCurrQty(MaterialsVO updateData);

    /**
     * 🔒 동시성 제어를 위한 마지막 입고코드 조회 (FOR UPDATE)
     * 
     * @param yearMonth YYYYMM 형태
     * @return 해당 년월의 마지막 입고코드 (행 잠금)
     */
    String getLastMateInboCodeForUpdate(@Param("yearMonth") String yearMonth);

    /**
     * 🔒 임시 입고코드 예약 (동시성 방지)
     * 
     * @param tempRecord 임시 예약 데이터
     */
    void insertTempMateInboReservation(MaterialsVO tempRecord);

    void updatePurchaseOrderCurrQtyAndStatus(MaterialsVO updateData);


    // 자재 입출고 내역 조회
    List<MaterialsVO> selectMaterialFlowList(MaterialsVO search);

    /**
     * @param searchParams 검색 조건이 담긴 MaterialsVO
     * @return 자재 재고 목록
     */
    List<MaterialsVO> getMaterialStockList(MaterialsVO searchParams);
}
