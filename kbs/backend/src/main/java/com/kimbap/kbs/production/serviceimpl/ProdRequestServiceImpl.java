package com.kimbap.kbs.production.serviceimpl;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kimbap.kbs.production.mapper.ProdRequestMapper;
import com.kimbap.kbs.production.service.ProdRequestDetailVO;
import com.kimbap.kbs.production.service.ProdRequestFullVO;
import com.kimbap.kbs.production.service.ProdRequestService;
import com.kimbap.kbs.production.service.ProdRequestVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProdRequestServiceImpl implements ProdRequestService {
  
  @Autowired  
  private final ProdRequestMapper mapper;

  // 생산요청 조건 검색
  @Override
    public List<ProdRequestVO> getRequestByCondition(ProdRequestVO condition) {
    return mapper.selectProdRequestByCondition(condition);
  }
  // 생산요청코드별 생산요청상세 조회
  @Override
  public List<ProdRequestDetailVO> getDetailsByReqCd(String produReqCd) {
    return mapper.selectDetailsByProduReqCd(produReqCd);
  }
  // 생산요청 및 상세 저장
  @Override
  @Transactional
  public void saveProdPeq(ProdRequestFullVO fullVO) {
    ProdRequestVO request = fullVO.getRequest();
    List<ProdRequestDetailVO> details = fullVO.getReqDetails();

    boolean isNew = (request.getProduReqCd() == null || request.getProduReqCd().isEmpty());
    String produReqCd = isNew ? mapper.getNewProduReqCd() : request.getProduReqCd();
    request.setProduReqCd(produReqCd);

    if (isNew) {
        mapper.insertProductionReq(request);
    } else {
        mapper.updateProductionReq(request);
    }

    // 기존 상세 목록 가져오기
    List<ProdRequestDetailVO> existingDetails = mapper.selectDetailsByProduReqCd(produReqCd);
    Set<String> incomingProduProdCds = details.stream()
                                        .map(ProdRequestDetailVO::getProduProdCd)
                                        .filter(Objects::nonNull)
                                        .collect(Collectors.toSet());

    // 삭제 대상만 삭제
    for (ProdRequestDetailVO exist : existingDetails) {
      if (!incomingProduProdCds.contains(exist.getProduProdCd())) {
          mapper.deleteProdReqDetail(exist.getProduProdCd());
      }
    }

    // 신규 또는 수정 분기 처리
    for (ProdRequestDetailVO detail : details) {
      detail.setProduReqCd(produReqCd);

      if (detail.getProduProdCd() == null || detail.getProduProdCd().isEmpty()) {
          detail.setProduProdCd(mapper.getNewProduProdCd());
          mapper.insertProdReqDetail(detail);
      } else {
          mapper.updateProdReqDetail(detail);
      }
    }
  }
  // 생산요청과 관련 상세 삭제
  @Transactional
  @Override
  public void deleteProdReq(String produReqCd) {
      mapper.deleteProdReqDetailByReqCd(produReqCd);
      mapper.deleteProductionReq(produReqCd);
  }
}
