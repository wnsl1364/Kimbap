package com.kimbap.kbs.production.mapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.kimbap.kbs.production.service.BomDetailVO;
import com.kimbap.kbs.production.service.MateSupplierVO;
import com.kimbap.kbs.production.service.MaterVO;
import com.kimbap.kbs.production.service.MrpDetailVO;
import com.kimbap.kbs.production.service.MrpVO;
import com.kimbap.kbs.production.service.ProdPlanDetailVO;
import com.kimbap.kbs.production.service.ProdPlanVO;
import com.kimbap.kbs.production.service.ProdsVO;
import com.kimbap.kbs.production.service.PurcOrdDetailVO;
import com.kimbap.kbs.production.service.PurcOrdVO;

public interface ProdPlanMapper {
    // 코드 생성용 함수 호출
    String getNewProdPlanCd(); // 생산계획 PK
    String getNewPpdcode();    // 생산계획상세 PK
    
    // 검색 기능 =========================================
    List<ProdPlanVO> selectProdPlans();                                  // 전체 생산계획 조회
    List<ProdPlanVO> selectProdPlansByCondition(ProdPlanVO condition);   // 생산계획 조건 검색
    List<ProdPlanDetailVO> selectDetailsByPlanCd(String produPlanCd);    // 생산계획상세 검색
    List<ProdsVO> selectAllProducts();                                   // 제품기준정보 ALL 검색
    // 저장 기능 =========================================
    int insertProdPlan(ProdPlanVO plan);                                 // 생산계획 등록
    int insertProdPlanDetail(ProdPlanDetailVO detail);                   // 생산계획상세 등록
    void deleteProdPlanDetail(String ppdcode);                           // 생산계획 상세 삭제(수정 과정에서 삭제)
    void updateProdPlan(ProdPlanVO plan);                                // 생산계획 업데이트
    void updateProdPlanDetail(ProdPlanDetailVO detail);                  // 생산계획 상세 업데이트
    // 삭제 기능 ========================================
    void deleteProdPlanDetailByPlanCd(String produPlanCd);               // 생산계획코드 조건으로 상세 삭제
    void deleteProdPlan(String produPlanCd);                             // 생산계획 삭제

    // MRP 기능 =========================================
    String getNewMrpCd();                                                // MRP PK코드 생성
    void insertMrp(MrpVO vo);                                            // MRP 등록
    void insertMrpDetail(MrpDetailVO vo);                                // MRP 상세 등록
    List<BomDetailVO> selectBomMaterials(                                // 제품별 필요 자재 조회
        @Param("pcode") String pcode,
        @Param("prodVerCd") String prodVerCd
    );
    BigDecimal selectTotalStockByMate(                                   // 필요한 자재수량 조회
        @Param("mcode") String mcode,
        @Param("mateVerCd") String mateVerCd
    );

    // 발주서 자동 등록 기능 ==============================
    // 발주서 관련 메소드
    String getNewPurcCd();                                              // 발주서 PK 생성
    String getNewPurcDCd();                                             // 발주서상세 PK 생성
    void insertPurchaseOrder(PurcOrdVO vo);                             // 발주서 등록
    void insertPurchaseOrderDetail(PurcOrdDetailVO vo);                 // 발주서상세 등록
    void updatePurchaseOrderTotalAmount(                                // 발주서 총액 업데이트
        @Param("purcCd") String purcCd, 
        @Param("totalAmount") BigDecimal totalAmount
    );
    
    // MRP 기반 발주서 생성용 조회 메소드
    List<MrpDetailVO> selectMrpDetailsByMrpCd(String mrpCd);            // MRP 상세 조회
    MateSupplierVO selectBestSupplierByMaterial(                        // 최저가 공급업체 조회
        @Param("mcode") String mcode, 
        @Param("mateVerCd") String mateVerCd
    );
    MaterVO selectMaterialInfo(                                         // 자재 기본정보 조회
        @Param("mcode") String mcode, 
        @Param("mateVerCd") String mateVerCd
    );
    // 생산계획코드로 최신 MRP 코드 조회 (필요 시 사용)
    String selectLatestMrpCdByProdPlan(String produPlanCd);
    
    // 특정 기간 내 생성된 MRP 목록 조회 (관리용)
    List<MrpVO> selectMrpListByDateRange(@Param("startDate") LocalDate startDate, 
                                        @Param("endDate") LocalDate endDate);

}
