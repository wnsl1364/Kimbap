package com.kimbap.kbs.materials.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.kimbap.kbs.materials.service.StockMovementVO;

public interface StockMovementMapper {

    // ========== 이동요청서 등록 관련 ==========
    
    // 이동요청서 등록 (MOVE_REQ 테이블)
    void insertMoveRequest(StockMovementVO stockMovement);
    
    // 이동요청상세 등록 (MOVE_REQ_D 테이블)
    void insertMoveRequestDetail(StockMovementVO stockMovement);
    
    // 이동요청코드 시퀀스 조회
    int getLastMoveReqSequence(@Param("datePattern") String datePattern);
    
    // 이동요청상세코드 시퀀스 조회
    int getLastMoveReqDetailSequence(@Param("datePattern") String datePattern);

    // ========== 이동요청 목록 조회 관련 ==========
    
    // 이동요청 목록 전체 조회
    List<StockMovementVO> getAllMoveRequestList();
    
    // 이동요청 목록 검색 조회
    List<StockMovementVO> searchMoveRequestList(StockMovementVO searchParam);
    
    // 이동요청 단건 조회
    StockMovementVO getMoveRequestById(@Param("moveReqCd") String moveReqCd);
    
    // 이동요청 상세 목록 조회
    List<StockMovementVO> getMoveRequestDetailList(@Param("moveReqCd") String moveReqCd);

    // ========== 품목 선택 모달 관련 ==========
    
    // 재고가 있는 자재 목록 조회
    List<StockMovementVO> getAvailableMaterialList(@Param("fcode") String fcode);
    
    // 특정 품목의 재고 정보 조회
    StockMovementVO getItemStockInfo(@Param("itemType") String itemType, 
                                     @Param("itemCode") String itemCode, 
                                     @Param("lotNo") String lotNo);

    // ========== 도착위치 선택 모달 관련 ==========
    
    // 활성화된 공장 목록 조회
    List<StockMovementVO> getActiveFactoryList();
    
    // 특정 공장의 창고 목록 조회
    List<StockMovementVO> getWarehousesByFactory(@Param("fcode") String fcode);
    
    // 특정 창고의 구역 목록 조회
    List<StockMovementVO> getAreasByWarehouse(@Param("wcode") String wcode);

    // ========== 승인/거절 처리 관련 ==========
    
    // 이동요청 승인 처리
    void approveMoveRequest(StockMovementVO stockMovement);
    
    // 이동요청 거절 처리
    void rejectMoveRequest(StockMovementVO stockMovement);
    
    // 이동요청 상태 변경
    void updateMoveRequestStatus(@Param("moveReqCd") String moveReqCd, 
                                @Param("moveStatus") String moveStatus,
                                @Param("appr") String appr,
                                @Param("retuRea") String retuRea);

    // ========== 이동처리 실행 관련 ==========
    
    // 창고이동이력 등록
    void insertWareMoveHistory(StockMovementVO stockMovement);
    
    // 창고재고 업데이트 (출발지 재고 차감)
    void updateStockDecrease(StockMovementVO stockMovement);
    
    // 창고재고 업데이트 (도착지 재고 증가)
    void updateStockIncrease(StockMovementVO stockMovement);

    // ========== 유효성 검증 관련 ==========
    
    // 현재 재고량 조회
    Integer getCurrentStock(@Param("wareAreaCd") String wareAreaCd, 
                           @Param("itemType") String itemType,
                           @Param("itemCode") String itemCode, 
                           @Param("lotNo") String lotNo);
    
    // 도착지 구역 적재 가능 여부 검증
    Map<String, Object> validateDestinationArea(@Param("wareAreaCd") String wareAreaCd, 
                                               @Param("itemType") String itemType,
                                               @Param("itemCode") String itemCode,
                                               @Param("moveQty") Integer moveQty);

    // ========== 공통 조회 관련 ==========
    
    // 사용자 정보 조회
    StockMovementVO getUserInfo(@Param("empCd") String empCd);
    
    // 자재 정보 조회
    StockMovementVO getMaterialInfo(@Param("mcode") String mcode, 
                                   @Param("mateVerCd") String mateVerCd);
    
    // 제품 정보 조회
    StockMovementVO getProductInfo(@Param("pcode") String pcode, 
                                  @Param("prodVerCd") String prodVerCd);
    
    // 창고 정보 조회
    StockMovementVO getWarehouseInfo(@Param("wcode") String wcode, 
                                    @Param("wareVerCd") String wareVerCd);
    
    // 구역 정보 조회
    StockMovementVO getAreaInfo(@Param("wareAreaCd") String wareAreaCd);

    // ========== 통계/대시보드 관련 ==========
    
    // 이동요청 상태별 건수 조회
    Map<String, Integer> getMoveRequestStatusCount();
    
    // 월별 이동요청 건수 조회
    List<Map<String, Object>> getMonthlyMoveRequestCount(@Param("year") String year);
}