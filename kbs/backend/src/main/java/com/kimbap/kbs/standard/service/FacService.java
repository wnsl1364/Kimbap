package com.kimbap.kbs.standard.service;

import java.util.List;
import java.util.Map;

public interface FacService {
    List<FacVO> getFacList(); // 공장 목록
    void insertFacWithMax(FacVO fac); // 공장 등록 최대생산량 포함
    Map<String, Object> getFacDetail(String fcode); // 단건 조회 최대생산량 포함
    void updateFactory(FacVO fac);  // 수정
    List<ChangeItemVO> getChangeHistory(String fcode); // 변경 항목 이력
}
