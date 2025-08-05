package com.kimbap.kbs.production.serviceimpl;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kimbap.kbs.common.exception.InsufficientStockException;
import com.kimbap.kbs.production.mapper.ProdRequestMapper;
import com.kimbap.kbs.production.service.BomDetailVO;
import com.kimbap.kbs.production.service.MateReleaseVO;
import com.kimbap.kbs.production.service.ProdInboundVO;
import com.kimbap.kbs.production.service.ProdRequestDetailVO;
import com.kimbap.kbs.production.service.ProdRequestFullVO;
import com.kimbap.kbs.production.service.ProdRequestService;
import com.kimbap.kbs.production.service.ProdRequestVO;
import com.kimbap.kbs.production.service.WaStockVO;

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
    String mname = request.getRequ();  // 출고담당자 (또는 요청자명)
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
        // 자재출고 처리
        String produProdCd = detail.getProduProdCd();
        String pcode = detail.getPcode();
        String prodVerCd = detail.getProdVerCd();
        Integer reqQty = detail.getReqQty();
        // 제품입고 처리 시 필요값
        String fcode = request.getFcode();
        String facVerCd = request.getFacVerCd();

        List<BomDetailVO> materials = mapper.selectBomMaterials(pcode, prodVerCd);
        for (BomDetailVO material : materials) {
          BigDecimal totalNeedQty =  BigDecimal.valueOf(reqQty.intValue()).multiply(material.getNeedQty());
          List<WaStockVO> stocks = mapper.selectAvailableStocks(material.getMcode(), material.getMateVerCd());

          for (WaStockVO stock : stocks) {
            if (totalNeedQty.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal useQty = stock.getQty().min(totalNeedQty);
            String mcode = material.getMcode();
            String relType = "";
            if (mcode != null && mcode.length() >= 7) {
              String typeDigit = mcode.substring(4, 5);
              if ("1".equals(typeDigit)) relType = "h1";
              else if ("2".equals(typeDigit)) relType = "h2";
            }
            MateReleaseVO rel = new MateReleaseVO();
            rel.setMateRelCd(mapper.getNewMateRelCd());
            rel.setProduProdCd(produProdCd);
            rel.setMcode(material.getMcode());
            rel.setMateVerCd(material.getMateVerCd());
            rel.setWslcode(stock.getWslcode());
            rel.setLotNo(stock.getLotNo());
            rel.setRelQty(useQty);
            rel.setUnit(material.getUnit());
            rel.setRelDt(LocalDate.now());
            rel.setRelType(relType);
            rel.setMname(mname);
            rel.setCreDt(LocalDate.now());
            mapper.insertMateRel(rel);

            mapper.decreaseWareStock(stock.getWslcode(), useQty);
            totalNeedQty = totalNeedQty.subtract(useQty);
          }

          if (totalNeedQty.compareTo(BigDecimal.ZERO) > 0) {
            throw new InsufficientStockException("자재 재고 부족: " + material.getMcode());
          }
      }

      // 제품입고 처리
      ProdInboundVO inbo = new ProdInboundVO();
      inbo.setProdInboCd(createNewProdInboCd());
      inbo.setLotNo(mapper.getNewLotNo300());
      inbo.setPcode(pcode);
      inbo.setProdVerCd(prodVerCd);
      inbo.setInboQty(reqQty);
      inbo.setProduProdCd(produProdCd);
      inbo.setInboStatus("b4");
      inbo.setInboDt(Timestamp.valueOf(LocalDateTime.now()));
      inbo.setFcode(fcode);
      inbo.setFacVerCd(facVerCd);
      mapper.insertProdInbo(inbo);

    }
  }
  // 생산요청과 관련 상세 삭제
  @Transactional
  @Override
  public void deleteProdReq(String produReqCd) {
    mapper.deleteProdReqDetailByReqCd(produReqCd);
    mapper.deleteProductionReq(produReqCd);
  }
  
  public String createNewProdInboCd() {
      int latestSeq = mapper.selectTodayProdInboSeq(); // 가장 최근 일련번호
      int newSeq = latestSeq + 1;

      String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
      String pk = String.format("IN-%s-%04d", today, newSeq); // 예: IN-20250804-0004

      return pk;
  }

}
