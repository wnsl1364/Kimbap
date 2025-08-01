package com.kimbap.kbs.dashboard.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kimbap.kbs.dashboard.service.ChartService;
import com.kimbap.kbs.dashboard.service.ChartVO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class ChartController {

    private final ChartService chartService;

    @GetMapping("/chartData")
    public ChartVO getChartData() {
        return chartService.getChartData();
    }

    @GetMapping("/pieData")
    public List<ChartVO> getPieData() {
        return chartService.getPieData();
    }

    @GetMapping("/barData")
    public List<ChartVO> getBarData() {
        return chartService.getBarData();
    }

    @GetMapping("/orderData")
    public List<ChartVO> getOrderData() {
        return chartService.getOrderData();
    }

    
}
