package com.kimbap.kbs.standard.service;

import java.util.List;
import java.util.Map;

public interface MatService {
    List<MatVO> getMatList();
    void insertMatWithSuppliers(MatVO mat);
    Map<String, Object> getMaterialDetail(String mcode);
    List<MatVO> selectMatHistory(String mcode); // 자재기준정보 이력조회
    void updateMaterial(MatVO mat);// 여기 추가!
    List<ChangeItemVO> getChangeHistory(String mcode);  // 변경 항목 리스트 조회
}