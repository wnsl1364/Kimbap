package com.kimbap.kbs.standard.mapper;

import java.util.List;

import com.kimbap.kbs.standard.service.MatSupplierVO;
import com.kimbap.kbs.standard.service.MatVO;

public interface MatMapper  {
	List<MatVO> getMatList(); // 자재 목록 조회
    void insertMat(MatVO mat); // 자재 등록
    void insertMatSupplier(MatSupplierVO supplier); // 공급사 1건 등록
	int getMaxRawMaterialCode();     // 원자재 최대코드 조회
	int getMaxSubMaterialCode();     // 부자재 최대코드 조회
}
