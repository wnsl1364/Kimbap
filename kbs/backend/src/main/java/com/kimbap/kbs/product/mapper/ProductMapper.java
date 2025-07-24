package com.kimbap.kbs.product.mapper;

import java.util.List;

import com.kimbap.kbs.product.service.ProductVO;

public interface  ProductMapper {
  List<ProductVO> getProductList();
}
