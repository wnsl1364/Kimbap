package com.kimbap.kbs.standard.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.kimbap.kbs.standard.service.MatSupplierVO;
import com.kimbap.kbs.standard.service.MatVO;

public interface MatMapper  {
	List<MatVO> getMatList(); // 자재 목록 조회
    void insertMat(MatVO mat); // 자재 등록
    void insertMatSupplier(MatSupplierVO supplier); // 공급사 1건 등록
	boolean existsMcode(String mcode);
	MatVO getMatDetail(String mcode); // 자재 단건 조회
	List<MatSupplierVO> getMatSuppliers(String mcode); // 해당자재의 공급사 조회
	List<MatVO> selectMatHistory(String mcode); // 자재기준정보 이력조회
	MatVO selectLatestVersion(String mcode); // 최신 버전 조회
	int disableOldVersion(String mcode); // 기존 버전 비활성화
	int getNextRawMaterialCodeBySeq();
	int getNextSubMaterialCodeBySeq();
	int getSupplierCountByMcode(String mcode); // 자재별공급사 코드
	List<MatSupplierVO> selectAllSuppliersByMcode(String mcode); // 
	void updateIsUsedOnly(String mcode, String mateVerCd, String isUsed, String modi);
	void deleteMatSuppliersByMaterial(@Param("mcode") String mcode, @Param("mateVerCd") String mateVerCd);
}
