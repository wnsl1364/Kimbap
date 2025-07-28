package com.kimbap.kbs.standard.service;

import java.util.List;
import java.util.Map;

public interface MatService {
     List<MatVO> getMatList();                          // ✅ 자재 목록
    void insertMatWithSuppliers(MatVO mat);           // ✅ 등록 시 공급처 포함
    Map<String, Object> getMaterialDetail(String mcode); // ✅ 단건 + 공급처 같이 조회
    List<MatVO> selectMatHistory(String mcode);        // ✅ 전체 이력
    void updateMaterial(MatVO mat);                   // ✅ 수정
    List<ChangeItemVO> getChangeHistory(String mcode); // ✅ 변경 항목 이력
}