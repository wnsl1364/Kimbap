package com.kimbap.kbs.production.serviceimpl;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kimbap.kbs.production.mapper.FactoryMapper;
import com.kimbap.kbs.production.service.FactoryService;
import com.kimbap.kbs.production.service.FactoryVO;

@Service
public class FactoryServiceImpl implements FactoryService {

    @Autowired
    private FactoryMapper factoryMapper;

    @Override
    public List<FactoryVO> getFactoryList() {
        return factoryMapper.getFactoryList();
    }

}
