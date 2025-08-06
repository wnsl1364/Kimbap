package com.kimbap.kbs.standard.serviceimpl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kimbap.kbs.standard.mapper.WhMapper;
import com.kimbap.kbs.standard.service.ChangeItemVO;
import com.kimbap.kbs.standard.service.VersionSyncService;
import com.kimbap.kbs.standard.service.WhDetailVO;
import com.kimbap.kbs.standard.service.WhService;
import com.kimbap.kbs.standard.service.WhVO;

@Service
public class WhServiceImpl implements WhService{
    
    @Autowired
    private WhMapper whMapper;
    
    @Autowired
    private VersionSyncService versionSyncService;

    // 창고 목록 조회
    @Override
    public List<WhVO> getWarehouseList() {
        return whMapper.getWarehouseList();
    }

    // 창고 등록
    @Transactional
    @Override
    public void insertWh(WhVO wh) {
        // 1. 시퀀스를 이용한 창고 코드 생성
        int next = whMapper.getNextRawWarehouseCodeBySeq();
        String wcode = "WARE-" + String.format("%03d", next);

        System.out.println("생성된 wcode : " + wcode);

        // 2. 중복 여부 확인
        if (whMapper.existsWcode(wcode) > 0 ) {
            throw new RuntimeException("이미 존재하는 창고 코드: " + wcode);
        }

        // 3. 창고 기본 버전 설정
        wh.setWcode(wcode);

        if (wh.getWareVerCd() == null || wh.getWareVerCd().isEmpty()) {
            wh.setWareVerCd("V001");
        }

        // 4. 등록자 정보 
        if (wh.getRegi() == null || wh.getRegi().isEmpty()) {
            wh.setRegi("admin");
        } 

        // 5. 창고 등록
        whMapper.insertWh(wh);
        System.out.println("등록된 VO : " + wh);

        // ✅ 6. 상세구역 자동 생성 (ware_d)
        int row = wh.getMaxRow();
        int col = wh.getMaxCol();
        int floor = wh.getMaxFloor();

        for (int r = 1; r <= row; r++) {
            for (int c = 1; c <= col; c++) {
                for (int f = 1; f <= floor; f++) {
                    WhDetailVO detail = new WhDetailVO();
                    
                    String wcodeNum = wcode.substring(wcode.lastIndexOf("-") + 1); // "001"
                    char rowChar = (char) ('A' + (r - 1)); // 숫자 → 문자
                    String areaCode = "W-" + wcodeNum + "-" + rowChar + c + "-" + f;

                    detail.setWareAreaCd(areaCode); // W-001-A1-1
                    detail.setAreaRow(r);           // DB에는 여전히 숫자
                    detail.setAreaCol(c);
                    detail.setAreaFloor(f);
                    detail.setIsUsed("f1");
                    detail.setVol(100);             // Integer 고정 용량
                    detail.setWcode(wcode);
                    detail.setWareVerCd(wh.getWareVerCd());

                    whMapper.insertWhDetail(detail);
                }
            }
        }
    }

    // 버전 코드 생성 함수 (V001 -> V002)
    private String getNextVersion(String currentVer) {
        int verNum = Integer.parseInt(currentVer.replace("V", ""));
        return String.format("V%03d", verNum + 1);
        
    }

    

    // 창고 단건 조회
    @Override
    public Map<String, Object> getWhdetail(String wcode) {
        Map<String, Object> result = new HashMap<>();

        WhVO warehouse = whMapper.getWhdetail(wcode);

        result.put("warehouse", warehouse);

        return result;
    }

    // 창고 이력 조회
    @Override
    public List<WhVO> selectWhHistory(String wcode) {
        return whMapper.selectWhHistory(wcode);
    }

    @Override
    public List<ChangeItemVO> getChangeHistory(String wcode) {
        List<WhVO> histories = whMapper.selectWhHistory(wcode);
        List<ChangeItemVO> changeItems = new ArrayList<>();

        for (int i = 0; i < histories.size() - 1; i++) {
            WhVO current = histories.get(i);
            WhVO prev = histories.get(i + 1);

            if (!Objects.equals(current.getWareName(), prev.getWareName())) {
                changeItems.add(new ChangeItemVO("창고명", prev.getWareName(), current.getWareName(),
                        current.getChaRea(), current.getWareVerCd(), current.getRegDt(), current.getModi()));
            }
            if (!Objects.equals(current.getAddress(), prev.getAddress())) {
                changeItems.add(new ChangeItemVO("주소", prev.getAddress(), current.getAddress(),
                        current.getChaRea(), current.getWareVerCd(), current.getRegDt(), current.getModi()));
            }
            if (!Objects.equals(current.getMaxRow(), prev.getMaxRow())) {
                changeItems.add(new ChangeItemVO("최대 행", String.valueOf(prev.getMaxRow()), String.valueOf(current.getMaxRow()),
                        current.getChaRea(), current.getWareVerCd(), current.getRegDt(), current.getModi()));
            }
            if (!Objects.equals(current.getMaxCol(), prev.getMaxCol())) {
                changeItems.add(new ChangeItemVO("최대 열", String.valueOf(prev.getMaxCol()), String.valueOf(current.getMaxCol()),
                        current.getChaRea(), current.getWareVerCd(), current.getRegDt(), current.getModi()));
            }
            if (!Objects.equals(current.getMaxFloor(), prev.getMaxFloor())) {
                changeItems.add(new ChangeItemVO("최대 층", String.valueOf(prev.getMaxFloor()), String.valueOf(current.getMaxFloor()),
                        current.getChaRea(), current.getWareVerCd(), current.getRegDt(), current.getModi()));
            }
            if (!Objects.equals(current.getNote(), prev.getNote())) {
                changeItems.add(new ChangeItemVO("비고", prev.getNote(), current.getNote(),
                        current.getChaRea(), current.getWareVerCd(), current.getRegDt(), current.getModi()));
            }
            if (!Objects.equals(current.getFacVerCd(), prev.getFacVerCd())) {
                changeItems.add(new ChangeItemVO("공장버전", prev.getFacVerCd(), current.getFacVerCd(),
                        current.getChaRea(), current.getWareVerCd(), current.getRegDt(), current.getModi()));
            }
            if (!Objects.equals(current.getFcode(), prev.getFcode())) {
                changeItems.add(new ChangeItemVO("공장코드", prev.getFcode(), current.getFcode(),
                        current.getChaRea(), current.getWareVerCd(), current.getRegDt(), current.getModi()));
            }
        }

        // V001 등록 이력
        if (!histories.isEmpty()) {
            WhVO first = histories.get(histories.size() - 1); // 마지막이 V001
            changeItems.add(new ChangeItemVO(
                    "초기등록", "-", "-", first.getChaRea() != null ? first.getChaRea() : "-",
                    first.getWareVerCd(),
                    first.getRegDt(),
                    first.getModi()
            ));
        }

        return changeItems;
    }

    // 창고 수정
    @Transactional
    @Override
    public void updateWh(WhVO newWh) {
        // 1. 기존 최신 버전 조회
        WhVO oldWh = whMapper.selectLatestVersion(newWh.getWcode());
        if (oldWh == null) {
            throw new RuntimeException("존재하지 않는 창고코드: " + newWh.getWcode());
        }

        // 2. 내용 변경 여부 판단 (isUsed는 제외)
        boolean isChanged =
                !Objects.equals(oldWh.getWareName(), newWh.getWareName()) ||
                !Objects.equals(oldWh.getWareType(), newWh.getWareType()) ||
                !Objects.equals(oldWh.getAddress(), newWh.getAddress()) ||
                !Objects.equals(oldWh.getFcode(), newWh.getFcode());

        if (isChanged) {
            // ✅ 내용 변경 → 버전 증가 + insert
            whMapper.disableOldVersion(newWh.getWcode());

            String nextVer = getNextVersion(oldWh.getWareVerCd());
            newWh.setWareVerCd(nextVer);
            newWh.setIsUsed("f1");
            newWh.setRegDt(Timestamp.valueOf(LocalDateTime.now()));
            newWh.setModi(newWh.getModi());
            newWh.setRegi(oldWh.getRegi()); // 등록자는 유지

            whMapper.insertWh(newWh);

            // ✅ 🔥 버전 변경에 따른 참조 테이블 동기화
            versionSyncService.syncWarehouseVersion(
                newWh.getWcode(),
                oldWh.getWareVerCd(),  // V001
                nextVer                // V002
            );

        } else if (!Objects.equals(oldWh.getIsUsed(), newWh.getIsUsed())) {
            // ✅ 사용여부만 변경 → update만 수행
            whMapper.updateIsUsedOnly(oldWh.getWcode(), oldWh.getWareVerCd(), newWh.getIsUsed(), newWh.getModi());
        } else {
            // ❌ 변경 없음
            System.out.println("⚠️ 창고 정보 변경 없음, 처리 생략");
        }
    }
}
