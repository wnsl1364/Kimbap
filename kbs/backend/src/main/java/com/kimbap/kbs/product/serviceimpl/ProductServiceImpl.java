package com.kimbap.kbs.product.serviceimpl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kimbap.kbs.product.mapper.ProductMapper;
import com.kimbap.kbs.product.service.ProductService;
import com.kimbap.kbs.product.service.ProductVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
  
  private final ProductMapper productMapper;
  
  // 제품 목록 조회
  @Override
  public List<ProductVO> getProductList() {
    return productMapper.getProductList();
  }

  // 기타 필요한 메소드 구현
  
}
