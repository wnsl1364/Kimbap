package com.kimbap.kbs.standard.service;

import java.util.List;
import java.util.Map;

public interface MatService {
    List<MatVO> getMatList();
    void insertMatWithSuppliers(MatVO mat);
    Map<String, Object> getMaterialDetail(String mcode);
}