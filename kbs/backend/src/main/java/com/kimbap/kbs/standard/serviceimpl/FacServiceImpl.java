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

import com.kimbap.kbs.standard.mapper.FacMapper;
import com.kimbap.kbs.standard.service.ChangeItemVO;
import com.kimbap.kbs.standard.service.FacMaxVO;
import com.kimbap.kbs.standard.service.FacService;
import com.kimbap.kbs.standard.service.FacVO;

@Service
public class FacServiceImpl  implements FacService{
    
    @Autowired
    private FacMapper facMapper;

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
        // 기존 최신 버전 조회
        FacVO oldFac = facMapper.selectLatestVersion(newFac.getFcode());
        if (oldFac == null) {
            throw new RuntimeException("존재하지 않는 공장코드: " + newFac.getFcode());
        }

        // ✅ 내용 변경 여부 (opStatus는 제외)
        boolean isChanged =
                !Objects.equals(oldFac.getFacName(), newFac.getFacName()) ||
                !Objects.equals(oldFac.getAddress(), newFac.getAddress()) ||
                !Objects.equals(oldFac.getTel(), newFac.getTel());

        if (isChanged) {
            // ✅ 실제 내용 변경 → 버전 증가
            facMapper.disableOldVersion(newFac.getFcode());

            String nextVer = getNextVersion(oldFac.getFacVerCd());
            newFac.setFacVerCd(nextVer);
            newFac.setRegDt(Timestamp.valueOf(LocalDateTime.now()));
            newFac.setModi(newFac.getModi());

            facMapper.insertFac(newFac);

            // 최대 생산량 정보 insert
            if (newFac.getFacMaxList() != null) {
                int index = 1;
                for (FacMaxVO facmax : newFac.getFacMaxList()) {
                    facmax.setFcode(newFac.getFcode());
                    facmax.setFacVerCd(newFac.getFacVerCd());

                    String maxProduCd = String.format("%s-%s-FAC-%02d", newFac.getFcode(), newFac.getFacVerCd(), index);
                    facmax.setMaxProduCd(maxProduCd);

                    facMapper.insertFacMax(facmax);
                    index++;
                }
            }

        } else if (!Objects.equals(oldFac.getOpStatus(), newFac.getOpStatus())) {
            // ✅ 내용 동일, 가동여부(opStatus)만 변경 → update만 수행
            facMapper.updateOpStatusOnly(oldFac.getFcode(), oldFac.getFacVerCd(), newFac.getOpStatus(), newFac.getModi());
        } else {
            // ❌ 아무것도 바뀐 게 없음
            System.out.println("⚠️ 공장 정보 변경 없음, 처리 생략");
        }
    }

    // 단건조회
    @Override
    public Map<String, Object> getFacDetail(String fcode) {
        Map<String, Object> result = new HashMap<>();

        FacVO factory = facMapper.getFacDetail(fcode);
        List<FacMaxVO> facMax = facMapper.getFacMaxs(fcode);

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
