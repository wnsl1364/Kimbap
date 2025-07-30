package com.kimbap.kbs.materials.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.kimbap.kbs.materials.service.MaterialsVO;
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
}
