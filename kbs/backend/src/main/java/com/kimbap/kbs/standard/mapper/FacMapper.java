package com.kimbap.kbs.standard.mapper;

import java.util.List;

import com.kimbap.kbs.standard.service.FacMaxVO;
import com.kimbap.kbs.standard.service.FacVO;

public interface FacMapper {
    List<FacVO> getFacList(); // 공장 목록 조회
    void insertFac(FacVO fac); // 공장 등록
    void insertFacMax(FacMaxVO max); // 공장별 최대 생산량 등록
    FacVO getFacDetail(String fcode); // 공장 단건 조회
    List<FacMaxVO> getFacMaxs(String fcode); // 해당 공장 최대생산량 조회
    List<FacVO> selectFacHistory(String fcode); // 공장기준정보 이력조회
    FacVO selectLatestVersion(String fcode); // 최신 버전 조회
    int disableOldVersion(String fcode); // 기존 버전 비활성화
    int getNextRawFactoryCodeBySeq(); // 공장 코드 시퀀스
    int getFacMaxCountByfcode(String fcode); // 공장별 최대 생산량 코드 시퀀스
    boolean existsFcode(String fcode);
    List<FacMaxVO> selectAllFacMaxByFcode(String fcode); //
    void updateOpStatusOnly(String fcode, String facVerCd, String opStatus, String modi); // 사용여부만 업데이트
}
