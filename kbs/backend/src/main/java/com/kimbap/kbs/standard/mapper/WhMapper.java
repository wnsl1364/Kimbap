package com.kimbap.kbs.standard.mapper;

import java.util.List;

import com.kimbap.kbs.standard.service.WhDetailVO;
import com.kimbap.kbs.standard.service.WhVO;

public interface WhMapper {
    List<WhVO> getWarehouseList(); // 창고 목록 조회
    void insertWh(WhVO wh); // 창고 등록
    WhVO getWhdetail(String wcode); // 창고 단건 조회
    int existsWcode(String wcode); // 창고 코드 존재 여부 확인
    List<WhVO> selectWhHistory(String wcode); // 창고 기준정보 이력조회
    WhVO selectLatestVersion(String wcode); // 최신 버전 조회
    int disableOldVersion(String wcode); // 기존 버전 비활성화
    int getNextRawWarehouseCodeBySeq(); // 시퀀스 
    void insertWhDetail(WhDetailVO detail); // 창고 상세 등록 
    void updateWh(WhVO wh); // 안 써도 정의만 하면 됨
    void updateIsUsedOnly(String wcode, String wareVerCd, String isUsed, String modi);
}
    