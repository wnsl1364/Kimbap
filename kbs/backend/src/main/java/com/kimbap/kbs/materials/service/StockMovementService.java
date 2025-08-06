package com.kimbap.kbs.materials.service;

import java.util.List;
import java.util.Map;

public interface StockMovementService {

    // ========== 이동요청서 등록 관련 ==========
    
    // 이동요청서 등록
    String registerMoveRequest(StockMovementVO stockMovement);
    
    // 이동요청 상세 목록 등록
    String registerMoveRequestDetails(List<StockMovementVO> detailList);
    
    // 이동요청서 전체 등록 (헤더 + 상세)
    String registerFullMoveRequest(StockMovementVO header, List<StockMovementVO> detailList);

    // ========== 이동요청 목록 조회 관련 ==========
    
    // 이동요청 목록 전체 조회
    List<StockMovementVO> getAllMoveRequestList();
    
    // 이동요청 목록 검색 조회
    List<StockMovementVO> searchMoveRequestList(StockMovementVO searchParam);
    
    // 이동요청 단건 조회
    StockMovementVO getMoveRequestById(String moveReqCd);
    
    // 이동요청 상세 목록 조회
    List<StockMovementVO> getMoveRequestDetailList(String moveReqCd);

    // ========== 품목 선택 모달 관련 ==========
    
    // 재고가 있는 자재 목록 조회
    List<StockMovementVO> getAvailableMaterialList(String fcode);
    
    // 특정 품목의 재고 정보 조회
    StockMovementVO getItemStockInfo(String itemType, String itemCode, String lotNo);

    // ========== 도착위치 선택 모달 관련 ==========
    
    // 활성화된 공장 목록 조회
    List<StockMovementVO> getActiveFactoryList();
    
    // 특정 공장의 창고 목록 조회
    List<StockMovementVO> getWarehousesByFactory(String fcode);
    
    // 특정 창고의 구역 목록 조회
    List<StockMovementVO> getAreasByWarehouse(String wcode);

    // ========== 승인/거절 처리 관련 ==========
    
    // 이동요청 승인 처리
    String approveMoveRequest(String moveReqCd, String approver, String comment);
    
    // 이동요청 거절 처리
    String rejectMoveRequest(String moveReqCd, String approver, String rejectReason);
    
    // 다중 이동요청 승인 처리
    String approveBatchMoveRequest(List<String> moveReqCdList, String approver, String comment);

    // ========== 이동처리 실행 관련 ==========
    
    // 승인된 이동요청 실행 처리
    String executeMoveRequest(String moveReqCd);
    
    // 다중 이동요청 실행 처리
    String executeBatchMoveRequest(List<String> moveReqCdList);

    // ========== 유효성 검증 관련 ==========
    
    // 이동 가능 여부 검증
    Map<String, Object> validateMoveRequest(StockMovementVO stockMovement);
    
    // 현재 재고량 조회
    Integer getCurrentStock(String wareAreaCd, String itemType, String itemCode, String lotNo);
    
    // 도착지 구역 적재 가능 여부 검증
    Map<String, Object> validateDestinationArea(String wareAreaCd, String itemType, String itemCode, Integer moveQty);

    // ========== 코드 생성 관련 ==========
    
    // 이동요청코드 생성
    String generateMoveReqCode();
    
    // 이동요청상세코드 생성
    String generateMoveReqDetailCode();

    // ========== 통계/대시보드 관련 ==========
    
    // 이동요청 상태별 건수 조회
    Map<String, Integer> getMoveRequestStatusCount();
    
    // 월별 이동요청 건수 조회
    List<Map<String, Object>> getMonthlyMoveRequestCount(String year);
    
    // 이동요청 승인 대기 건수 조회
    Integer getPendingApprovalCount();
}