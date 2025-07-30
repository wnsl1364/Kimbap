package com.kimbap.kbs.materials.serviceimpl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kimbap.kbs.materials.mapper.MateLoadingMapper;
import com.kimbap.kbs.materials.service.MateLoadingVO;
import com.kimbap.kbs.materials.service.MateLoadingService;

@Service
@Transactional
public class MateLoadingServiceImpl implements MateLoadingService {

    @Autowired
    private MateLoadingMapper mateLoadingMapper;

    @Override
    public List<MateLoadingVO> getAllMateLoadingWaitList() {
        List<MateLoadingVO> list = mateLoadingMapper.getAllMateLoadingWaitList();
        return list;
    }

    @Override
    public MateLoadingVO getMateLoadingByInboCd(String mateInboCd) {
        MateLoadingVO result = mateLoadingMapper.getMateLoadingByInboCd(mateInboCd);
        return result;
    }

    @Override
    public String processMateLoading(MateLoadingVO mateLoading) {
        // 현재 시간 설정
        mateLoading.setInboDt(Timestamp.valueOf(LocalDateTime.now()));
        
        // ware_stock 테이블에 적재 정보 저장
        mateLoadingMapper.insertWareStock(mateLoading);
        
        return "자재 적재 처리가 완료되었습니다.";
    }

    @Override
    public String processMateLoadingBatch(List<MateLoadingVO> mateLoadingList) {
        int successCount = 0;
        int failCount = 0;
        
        for (MateLoadingVO mateLoading : mateLoadingList) {
            try {
                // 현재 시간 설정
                mateLoading.setInboDt(Timestamp.valueOf(LocalDateTime.now()));
                
                // ware_stock 테이블에 적재 정보 저장
                mateLoadingMapper.insertWareStock(mateLoading);
                
                successCount++;
                
            } catch (Exception e) {
                failCount++;
            }
        }
        
        String result = String.format("다중 자재 적재 처리 완료 - 성공: %d건, 실패: %d건", 
                                     successCount, failCount);
        
        return result;
    }

    @Override
    public List<MateLoadingVO> getActiveFactoryList() {
        List<MateLoadingVO> factoryList = mateLoadingMapper.getActiveFactoryList();
        return factoryList;
    }

    @Override
    public String getWslCodeByArea(String wareAreaCd) {
        String wslCode = mateLoadingMapper.getWslCodeByArea(wareAreaCd);
        return wslCode;
    }
}