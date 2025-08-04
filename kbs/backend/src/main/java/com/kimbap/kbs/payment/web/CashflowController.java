package com.kimbap.kbs.payment.web;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kimbap.kbs.payment.service.CashflowService;
import com.kimbap.kbs.payment.service.CashflowVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/pay")
public class CashflowController {
    
    @Autowired
    private CashflowService cashflowService;

    // 입출금 내역 목록 조회
    @GetMapping("/list")
    public List<CashflowVO> getCashflows(@RequestParam Map<String, Object> params) {
        return cashflowService.getCashflowList(params);
    }

    // 입금 내역 조회
    @GetMapping("/income")
    public List<CashflowVO> getOnlyIncome() {
        return cashflowService.getOnlyIncomeList();
    }
    
    // 입출금 내역 등록
    @PostMapping("/insert")
    public Map<String, Object> insertCf(@RequestBody CashflowVO cf){
        try {
            cashflowService.insertCf(cf);
            return Map.of(
                "success", true,
                "message", "등록 성공"
            );
        } catch (Exception e) {
            log.error("입출금 등록 실패", e);
            return Map.of(
                "success", false,
                "message", "등록 실패: " + e.getMessage()
            );
        }
    }

    // 입출금 내역 단건 조회
    @GetMapping("/detail/{statementCd}")
    public Map<String,Object> getCfDetail(@PathVariable String statementCd){
        return cashflowService.getCfDetail(statementCd);
    }

    // 입출금 내역 수정
    @PutMapping("/update")
    public Map<String,Object> updateCf(@RequestBody CashflowVO cf){
        try {
            cashflowService.updateCf(cf);
            return Map.of(
                "success", true,
                "message", "수정 성공"
            );
        } catch(Exception e) {
            log.error("입출금 수정 실패", e);
            return Map.of(
                "success", false,
                "message", "수정 실패: " + e.getMessage()
            );
        }
    }
}
