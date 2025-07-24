package com.kimbap.kbs.product.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kimbap.kbs.product.service.ProductService;
import com.kimbap.kbs.product.service.ProductVO;

import lombok.RequiredArgsConstructor;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/list")
    public Map<String, Object> getProductList() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<ProductVO> list = productService.getProductList();
            result.put("result_code", "SUCCESS");
            result.put("message", "제품 목록 조회 성공");
            result.put("data", list);
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "제품 목록 조회 실패");
            result.put("data", e.getMessage());
        }
        return result;
    }
}
