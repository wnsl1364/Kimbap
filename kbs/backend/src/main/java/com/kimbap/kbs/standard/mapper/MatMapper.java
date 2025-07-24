package com.kimbap.kbs.standard.mapper;

import java.util.List;

import com.kimbap.kbs.standard.service.MatSupplierVO;
import com.kimbap.kbs.standard.service.MatVO;

public interface MatMapper  {
	List<MatVO> getMatList(); // 자재 목록 조회
    void insertMat(MatVO mat); // 자재 등록
    void insertMatSupplier(MatSupplierVO supplier); // 공급사 1건 등록
	int getNextRawMaterialCodeBySeq();
	int getNextSubMaterialCodeBySeq();
	boolean existsMcode(String mcode);
	MatVO getMatDetail(String mcode); // 자재 단건 조회
	List<MatSupplierVO> getMatSuppliers(String mcode); // 해당자재의 공급사 조회

}
