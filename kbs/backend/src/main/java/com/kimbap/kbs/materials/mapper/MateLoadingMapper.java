package com.kimbap.kbs.materials.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.kimbap.kbs.materials.service.MateLoadingVO;

public interface MateLoadingMapper {
 
  // 자재 적재 대기 목록 전체 조회()
  List<MateLoadingVO> getAllMateLoadingWaitList();
  // 단건조회
  MateLoadingVO getMateLoadingByInboCd(@Param("mateInboCd") String mateInboCd);
  // 적재 처리
  void insertWareStock(MateLoadingVO mateLoading);
  // 활성화된 공장 목록 조회 (드롭다운)
  List<MateLoadingVO> getActiveFactoryList();

  //------------------------------------------------------------------------------
  //특정 공장의 창고 목록 조회 (창고 유형별)
  List<MateLoadingVO> getWarehousesByFactory(@Param("fcode") String fcode);
  //특정 창고의 구역 정보 조회(층별)
  List<MateLoadingVO> getWarehouseAreasByFloor(@Param("wcode") String wcode, @Param("floor") Integer floor);
  //구역별 현재 적재 상황 조회
  MateLoadingVO getWarehouseAreaStock(@Param("wareAreaCd") String wareAreaCd);
  //창고구역코드 존재 여부 확인
  String getWareAreaCode(@Param("wcode") String wcode, 
                        @Param("areaRow") String areaRow, 
                        @Param("areaCol") Integer areaCol, 
                        @Param("areaFloor") Integer areaFloor);
  //창고재고목록 코드 생성을 위한 순번 조회
  int getLastWareStockSequence(@Param("datePattern") String datePattern);
  //구역별 현재 적재량 합계 조회
  Integer getCurrentVolumeByArea(@Param("wareAreaCd") String wareAreaCd);
  //구역에 적재된 자재코드 조회(동일자재 체크용)
  String getCurrentMaterialByArea(@Param("wareAreaCd") String wareAreaCd);
  //동일한 자재가 적재된 다른 구역들 조회(분할적재용)
  List<MateLoadingVO> getSameMaterialAreas(@Param("mcode") String mcode, 
                                        @Param("fcode") String fcode,
                                        @Param("excludeAreaCd") String excludeAreaCd);
  
  // material 테이블에서 자재 정보 조회 (item_type, unit 등)
  MateLoadingVO getMaterialInfo(@Param("mcode") String mcode);
  
  // 적재 처리 시 loaded_qty 업데이트 및 상태 변경 (c8 적재중, c9 적재완료)
  void updateLoadedQtyAndStatus(@Param("mateInboCd") String mateInboCd, @Param("loadedQty") Integer loadedQty);
}
