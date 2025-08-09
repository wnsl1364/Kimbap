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

    // 제품입고 처리 시 필요값
    String fcode = request.getFcode();
    String facVerCd = request.getFacVerCd();

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

        // BOM 조회
        List<BomDetailVO> materials = mapper.selectBomMaterials(pcode, prodVerCd);
        for (BomDetailVO material : materials) {
            // 총 필요수량 = 요청수량 * 소요량
            BigDecimal remaining = BigDecimal.valueOf(reqQty).multiply(material.getNeedQty());

            // mcode + mateVerCd 기준 LOT FIFO 조회
            List<WaStockVO> stocks = mapper.selectAvailableStocks(material.getMcode(), material.getMateVerCd());

            for (WaStockVO stock : stocks) {
                if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;

                // 이 행에서 뺄 양 = min(행재고, 남은수량)
                BigDecimal delta = stock.getQty().min(remaining);
                if (delta.compareTo(BigDecimal.ZERO) <= 0) continue;

                // 음수 방지 가드(AND qty >= :delta)가 있는 UPDATE
                int updated = mapper.decreaseWareStock(stock.getWslcode(), delta);
                if (updated == 0) {
                    // 경쟁 등으로 수량 변동 → 현재 수량 재조회 후 가능한 만큼만 재시도
                    BigDecimal cur = mapper.selectQtyByWslcode(stock.getWslcode());
                    if (cur != null && cur.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal retry = cur.min(delta);
                        if (retry.compareTo(BigDecimal.ZERO) > 0) {
                            mapper.decreaseWareStock(stock.getWslcode(), retry);
                            insertMateRel(produProdCd, material, stock, retry, mname, resolveRelType(material.getMcode()));
                            remaining = remaining.subtract(retry);
                        }
                    }
                    continue;
                }

                // 출고 이력 기록
                insertMateRel(produProdCd, material, stock, delta, mname, resolveRelType(material.getMcode()));
                remaining = remaining.subtract(delta);
            }

            if (remaining.compareTo(BigDecimal.ZERO) > 0) {
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
  // ===== Helper: 출고 이력 Insert =====
  private void insertMateRel(String produProdCd,
                            BomDetailVO material,
                            WaStockVO stock,
                            BigDecimal relQty,
                            String mname,
                            String relType) {
    MateReleaseVO rel = new MateReleaseVO();
    rel.setMateRelCd(mapper.getNewMateRelCd());
    rel.setProduProdCd(produProdCd);
    rel.setMcode(material.getMcode());
    rel.setMateVerCd(material.getMateVerCd());
    rel.setWslcode(stock.getWslcode());
    rel.setLotNo(stock.getLotNo());
    rel.setRelQty(relQty);
    rel.setUnit(material.getUnit());
    rel.setRelDt(LocalDate.now());
    rel.setRelType(relType);
    rel.setMname(mname);
    rel.setCreDt(LocalDate.now());
    mapper.insertMateRel(rel);
  }

  // ===== Helper: 자재 유형 → relType =====
  private String resolveRelType(String mcode) {
    if (mcode != null && mcode.length() >= 5) {
      String typeDigit = mcode.substring(4, 5);
      if ("1".equals(typeDigit)) return "y1";
      if ("2".equals(typeDigit)) return "y1";
    }
    return "y1";
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
