package com.kimbap.kbs.standard.service;

import java.util.List;

public interface MatService {
    List<MatVO> getMatList();
    void insertMatWithSuppliers(MatVO mat);
}