package com.kimbap.kbs.standard.serviceimpl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kimbap.kbs.standard.mapper.FacMapper;
import com.kimbap.kbs.standard.service.ChangeItemVO;
import com.kimbap.kbs.standard.service.FacMaxVO;
import com.kimbap.kbs.standard.service.FacService;
import com.kimbap.kbs.standard.service.FacVO;
import com.kimbap.kbs.standard.service.VersionSyncService;

@Service
public class FacServiceImpl  implements FacService{
    
    @Autowired
    private FacMapper facMapper;

    @Autowired
    private VersionSyncService versionSyncService;

    // 목록 조회
    @Override
    public List<FacVO> getFacList(){
        return facMapper.getFacList();
    }

    // 등록
    @Transactional
    @Override
    public void insertFacWithMax(FacVO fac) {
        // 공장 코드 생성
        int next = facMapper.getNextRawFactoryCodeBySeq();
        String fcode = String.format("FAC-%03d", next);
        System.out.println("생성된 fcode : " + fcode);

        // 중복 확인
        if (facMapper.existsFcode(fcode)) {
            throw new RuntimeException("이미 존재하는 공장코드: " + fcode);
        }

        fac.setFcode(fcode);

        // 버전 기본값
        if (fac.getFacVerCd() == null || fac.getFacVerCd().isEmpty()) {
            fac.setFacVerCd("V001");
        }

        // 4. 등록자 정보
        if (fac.getRegi() == null || fac.getRegi().isEmpty()) {
            fac.setRegi("admin");
        }

        // 공장 등록
        facMapper.insertFac(fac);
        System.out.println("등록되는 VO: " + fac);

        // 최대 생산량 등록
        if (fac.getFacMaxList() != null) {
            int index = facMapper.getFacMaxCountByfcode(fcode);
            for (FacMaxVO facMax : fac.getFacMaxList()) {
                facMax.setFcode(fcode);
                facMax.setFacVerCd(fac.getFacVerCd());

                // 최대 생산 코드 생성 
                String maxProduCd = String.format("%s-MAX-%02d", fcode, index + 1);
                facMax.setMaxProduCd(maxProduCd); // ✅ 필드명 정확히 사용
                System.out.println("📌 FacMaxVO 확인: " + facMax); // 실제 넘어가는 값
                facMapper.insertFacMax(facMax);
                System.out.println("✅ insertFacMax 호출 완료");
                index++;
            }
        }
    }

    // 버전 코드 생성 함수 (V001 -> V002)
    private String getNextVersion(String currentVer) {
        int verNum = Integer.parseInt(currentVer.replace("V", ""));
        return String.format("V%03d", verNum + 1);
    }

    // 수정
    @Transactional
    @Override
    public void updateFactory(FacVO newFac) {
        // 1) 기존 최신 버전 조회
        FacVO oldFac = facMapper.selectLatestVersion(newFac.getFcode());
        if (oldFac == null) {
            throw new RuntimeException("존재하지 않는 공장코드: " + newFac.getFcode());
        }

        // 2) 기존 FacMax(자식) 목록 조회
        List<FacMaxVO> oldFacMaxList = facMapper.selectFacMaxbyFactory(
                oldFac.getFcode(),
                oldFac.getFacVerCd()
        );

        // 3) 변경 여부 판단 (opStatus는 제외 → 자재의 기준정보 변경과 동일 개념)
        boolean isChanged =
                !Objects.equals(oldFac.getFacName(), newFac.getFacName()) ||
                !Objects.equals(oldFac.getAddress(), newFac.getAddress()) ||
                !Objects.equals(oldFac.getTel(), newFac.getTel());

        // 4) FacMax(자식) 변경 여부 판단 (자재의 suppliersChanged와 동일)
        boolean facMaxChanged = !isSameFacMax(oldFacMaxList, newFac.getFacMaxList());

        if (isChanged) {
            // ✅ 본체 내용 변경 → 버전 증가 (자재와 동일)
            // 기존 본체 비활성화
            facMapper.disableOldVersion(newFac.getFcode());

            // 기존 FacMax 비활성화 (자재의 updateSupplierIsUsed에 해당)
            facMapper.updateFacMaxIsUsed(oldFac.getFcode(), oldFac.getFacVerCd(), "f2");

            // 새 버전 생성
            String nextVer = getNextVersion(oldFac.getFacVerCd());
            newFac.setFacVerCd(nextVer);
            newFac.setRegDt(Timestamp.valueOf(LocalDateTime.now()));
            newFac.setModi(newFac.getModi());

            // 새 버전 본체 INSERT
            facMapper.insertFac(newFac);

            // 자식 테이블 버전 참조 동기화 (자재의 versionSyncService.syncMaterialVersion과 동일)
            versionSyncService.syncFactoryVersion(newFac.getFcode(), oldFac.getFacVerCd(), nextVer);

            // 공급사에 해당하는 FacMax 등록 (새 버전으로)
            if (newFac.getFacMaxList() != null && !newFac.getFacMaxList().isEmpty()) {
                insertFacMaxes(newFac, nextVer);
            }

        } else if (facMaxChanged) {
            // ✅ FacMax만 변경 → 구버전은 그대로, 자식만 교체 (자재의 suppliersChanged 분기와 동일)
            facMapper.updateFacMaxIsUsed(oldFac.getFcode(), oldFac.getFacVerCd(), "f2");
            insertFacMaxes(newFac, oldFac.getFacVerCd()); // 같은 버전에 f1로 재등록

        } else if (!Objects.equals(oldFac.getOpStatus(), newFac.getOpStatus())) {
            // ✅ 사용여부에 해당하는 운영상태만 변경 → 필드만 update (자재의 is_used만 변경 분기와 동일)
            facMapper.updateOpStatusOnly(oldFac.getFcode(), oldFac.getFacVerCd(),
                    newFac.getOpStatus(), newFac.getModi());

        } else {
            System.out.println("⚠️ 공장 정보 변경 없음, 처리 생략");
        }
    }

    /** FacMax 동일 여부 비교 메서드 (자재의 isSameSuppliers와 동일한 스타일: 인덱스/핵심필드 비교) */
    private boolean isSameFacMax(List<FacMaxVO> oldList, List<FacMaxVO> newList) {
        if (oldList == null) oldList = Collections.emptyList();
        if (newList == null) newList = Collections.emptyList();

        if (oldList.size() != newList.size()) return false;

        // 공장 최대생산량 식별/핵심 키 기준 비교 (자재의 cpCd/ltime과 유사)
        for (int i = 0; i < oldList.size(); i++) {
            FacMaxVO oldV = oldList.get(i);
            FacMaxVO newV = newList.get(i);

            // 예시: 제품코드 + 리드타임 비교 (필요시 unit/maxQty 등 추가)
            if (!Objects.equals(oldV.getPcode(), newV.getPcode())) return false;
            if (!Objects.equals(oldV.getMpqty(), newV.getMpqty())) return false;

            // 필요 시 추가 비교
            // if (!Objects.equals(oldV.getUnit(), newV.getUnit())) return false;
            // if (!Objects.equals(oldV.getMaxQty(), newV.getMaxQty())) return false;
        }
        return true;
    }

    /** FacMax INSERT 공통 처리 (자재의 insertSuppliers와 동일 패턴) */
    private void insertFacMaxes(FacVO fac, String ver) {
        int index = 1;
        if (fac.getFacMaxList() == null) return;

        for (FacMaxVO max : fac.getFacMaxList()) {
            max.setFcode(fac.getFcode());
            max.setFacVerCd(ver);
            String maxProduCd = String.format("%s-%s-FAC-%02d", fac.getFcode(), ver, index);
            max.setMaxProduCd(maxProduCd);

            facMapper.insertFacMax(max);
            index++;
        }
    }

    // 단건조회
    @Override
    public Map<String, Object> getFacDetail(String fcode) {
        Map<String, Object> result = new HashMap<>();

        FacVO factory = facMapper.getFacDetail(fcode);
        List<FacMaxVO> facMax = facMapper.selectFacMaxbyFactory(
            fcode,
            factory.getFacVerCd());

        result.put("factory", factory);
        result.put("facMax", facMax);

        return result;
    }

    // 변경이력조회
    @Override
    public List<ChangeItemVO> getChangeHistory(String fcode) {
        List<FacVO> histories = facMapper.selectFacHistory(fcode); // 최신 → 과거
        List<ChangeItemVO> changeItems = new ArrayList<>();

        List<FacMaxVO> facMax = facMapper.selectAllFacMaxByFcode(fcode);
        Map<String, List<FacMaxVO>> facMaxMap = new HashMap<>();
        for (FacMaxVO m : facMax) {
            facMaxMap.computeIfAbsent(m.getFacVerCd(), k -> new ArrayList<>()).add(m);
        }

        for (int i = 0; i < histories.size() - 1; i++) {
            FacVO current = histories.get(i);
            FacVO prev = histories.get(i + 1);

            List<FacMaxVO> currfacMax = facMaxMap.getOrDefault(current.getFacVerCd(), new ArrayList<>());
            List<FacMaxVO> prevfacMax = facMaxMap.getOrDefault(prev.getFacVerCd(), new ArrayList<>());

            // 🔹 제품 비교: 추가 / 변경 / 삭제
            for (FacMaxVO curr : currfacMax) {
                FacMaxVO matchedPrev = prevfacMax.stream()
                    .filter(p -> p.getPcode().equals(curr.getPcode()))
                    .findFirst()
                    .orElse(null);

                if (matchedPrev == null) {
                    // 신규 제품 추가
                    changeItems.add(new ChangeItemVO(
                        "제품 추가",
                        "-",
                        curr.getPcode() + " (버전: " + curr.getProdVerCd() + ", 생산량: " + curr.getMpqty() + ")",
                        current.getChaRea(), current.getFacVerCd(), current.getRegDt(), current.getModi()
                    ));
                } else {
                    if (!Objects.equals(curr.getMpqty(), matchedPrev.getMpqty())) {
                        changeItems.add(new ChangeItemVO(
                            "최대생산량 변경",
                            matchedPrev.getPcode() + ": " + matchedPrev.getMpqty(),
                            curr.getPcode() + ": " + curr.getMpqty(),
                            current.getChaRea(), current.getFacVerCd(), current.getRegDt(), current.getModi()
                        ));
                    }
                }
            }

            for (FacMaxVO old : prevfacMax) {
                boolean removed = currfacMax.stream()
                    .noneMatch(c -> c.getPcode().equals(old.getPcode()));

                if (removed) {
                    changeItems.add(new ChangeItemVO(
                        "제품 삭제",
                        old.getPcode() + " (버전: " + old.getProdVerCd() + ")",
                        "-",
                        current.getChaRea(), current.getFacVerCd(), current.getRegDt(), current.getModi()
                    ));
                }
            }

            // 🔹 공장 기준정보 필드 비교
            if (!Objects.equals(current.getFacName(), prev.getFacName())) {
                changeItems.add(new ChangeItemVO("공장명", prev.getFacName(), current.getFacName(),
                    current.getChaRea(), current.getFacVerCd(), current.getRegDt(), current.getModi()));
            }
            if (!Objects.equals(current.getAddress(), prev.getAddress())) {
                changeItems.add(new ChangeItemVO("주소", prev.getAddress(), current.getAddress(),
                    current.getChaRea(), current.getFacVerCd(), current.getRegDt(), current.getModi()));
            }
            if (!Objects.equals(current.getTel(), prev.getTel())) {
                changeItems.add(new ChangeItemVO("전화번호", prev.getTel(), current.getTel(),
                    current.getChaRea(), current.getFacVerCd(), current.getRegDt(), current.getModi()));
            }
            if (!Objects.equals(current.getMname(), prev.getMname())) {
                changeItems.add(new ChangeItemVO("담당자", prev.getMname(), current.getMname(),
                    current.getChaRea(), current.getFacVerCd(), current.getRegDt(), current.getModi()));
            }
            if (!Objects.equals(current.getNote(), prev.getNote())) {
                changeItems.add(new ChangeItemVO("비고", prev.getNote(), current.getNote(),
                    current.getChaRea(), current.getFacVerCd(), current.getRegDt(), current.getModi()));
            }
        }

        // 🔸 최초 등록 버전 넣기
        if (!histories.isEmpty()) {
            FacVO first = histories.get(histories.size() - 1); // 마지막이 V001
            changeItems.add(new ChangeItemVO(
                "-", "-", "-", "-",
                first.getFacVerCd(),
                first.getRegDt(),
                first.getModi()
            ));
        }

        return changeItems;
    }
    

}
