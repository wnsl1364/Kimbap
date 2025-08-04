package com.kimbap.kbs.payment.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kimbap.kbs.payment.service.UnpaidService;
import com.kimbap.kbs.payment.service.UnpaidSettleVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/unpaid")
@RequiredArgsConstructor
public class UnpaidController {

    private final UnpaidService unpaidService;

    @PostMapping("/update")
    public ResponseEntity<Void> settleUnpaid(@RequestBody UnpaidSettleVO vo) {
        unpaidService.settleUnpaid(vo);
        return ResponseEntity.ok().build();
    }
}
