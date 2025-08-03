package com.kimbap.kbs.production.mapper;

import java.math.BigDecimal;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.kimbap.kbs.production.service.BomDetailVO;
import com.kimbap.kbs.production.service.MateReleaseVO;
import com.kimbap.kbs.production.service.ProdInboundVO;
import com.kimbap.kbs.production.service.ProdRequestDetailVO;
import com.kimbap.kbs.production.service.ProdRequestVO;
import com.kimbap.kbs.production.service.WaStockVO;


public interface ProdRequestMapper {
    // 코드 생성용 함수 호출
    String getNewProduReqCd();     // 생산요청 PK
    String getNewProduProdCd();    // 생산요청상세 PK

    // 검색 기능 =========================================
    List<ProdRequestVO> selectProdRequestByCondition(ProdRequestVO condition);   // 생산요청 조건 검색
    List<ProdRequestDetailVO> selectDetailsByProduReqCd(String produReqCd);      // 생산요청상세 조회

    // 저장 기능 =========================================
    int insertProductionReq(ProdRequestVO request);                              // 생산요청 등록
    int insertProdReqDetail(ProdRequestDetailVO reqDetails);                     // 생산요청상세 등록
    void deleteProdReqDetail(String produProdCd);                                // 생산요청 상세 삭제(수정 과정에서 삭제)
    void updateProductionReq(ProdRequestVO request);                             // 생산요청 업데이트
    void updateProdReqDetail(ProdRequestDetailVO detail);                        // 생산요청 상세 업데이트

    // 삭제 기능 ========================================
    void deleteProdReqDetailByReqCd(String produReqCd);                         // 생산계획코드 조건으로 상세 삭제
    void deleteProductionReq(String produReqCd);                                // 생산계획 삭제

    // 생산 요청 시 자동 자재출고, 제품입고 기능 =========
    // BOM 자재 목록 조회
    List<BomDetailVO> selectBomMaterials(@Param("pcode") String pcode, @Param("prodVerCd") String prodVerCd);
    // 사용 가능한 자재재고 조회
    List<WaStockVO> selectAvailableStocks(@Param("mcode") String mcode, @Param("mateVerCd") String mateVerCd);
    // 재고 차감
    void decreaseWareStock(@Param("wslcode") String wslcode, @Param("useQty") BigDecimal useQty);
    // 자재출고 코드 생성 및 저장
    String getNewMateRelCd();
    void insertMateRel(MateReleaseVO vo);
    // 완제품 입고 코드 및 LOT 번호 생성
    String getNewProdInboCd();
    String getNewLotNo300();
    void insertProdInbo(ProdInboundVO vo);
    // ===================================================

}
