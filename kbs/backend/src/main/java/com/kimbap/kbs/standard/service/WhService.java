package com.kimbap.kbs.standard.service;

import java.util.List;
import java.util.Map;

public interface WhService {
    List<WhVO> getWarehouseList(); // 창고 목록 조회
    void insertWh(WhVO wh); // 창고 등록
    void updateWh(WhVO wh); // 창고 수정
    Map<String, Object> getWhdetail(String wcode); // 창고 단건 조회
    List<WhVO> selectWhHistory(String wcode); // 창고 이력 조회
    List<ChangeItemVO> getChangeHistory(String wcode); // 변경이력 조회
} 