package com.kimbap.kbs.standard.web;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kimbap.kbs.standard.service.ChangeItemVO;
import com.kimbap.kbs.standard.service.CompanyService;
import com.kimbap.kbs.standard.service.CompanyVO;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/std/cp")
public class CompanyController {
    
    @Autowired
    private CompanyService companyService;

    // 거래처 목록 조회
    @GetMapping("/list")
    public List<CompanyVO> getCompanyList(){
        return companyService.getCompanyList();
    }

    // 공급업체 목록 조회
    @GetMapping("/sup/list")
    public List<CompanyVO> getSupplierList(){
        return companyService.getSupplierList();
    }

    // 거래처 기준정보 등록
    @PostMapping("/insert")
    public Map<String, Object> insertCp(@RequestBody CompanyVO cp){
        try {
            companyService.insertCp(cp);
            return Map.of(
                "success", true,
                "message", "등록 성공"
            );
        } catch (Exception e) {
            return Map.of(
                "success", false,
                "message", "등록 실패: " + e.getMessage()
            );
        }
    }
    // 거래처 기준정보 단건조회
    @GetMapping("/detail/{cpCd}")
    public Map<String,Object> getCopanyDetail(@PathVariable String cpCd){
        return companyService.getCompanyDetail(cpCd);
    }

    // 거래처 기준정보 수정
    @PutMapping("/update")
    public ResponseEntity<Map<String,Object>> updateCompnay(@RequestBody CompanyVO cp)  {
        try {
            companyService.updateCp(cp);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "제품 정보가 성공적으로 수정되었습니다."
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = Map.of(
                "success", false,
                "message", "제품 수정 중 오류 발생: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error); // HTTP 500
        }
    }

    // 변경 이력조회
    @GetMapping("/change-history/{cpCd}")
    public List<ChangeItemVO> getChangeHistory(@PathVariable String cpCd){
        return companyService.getChangeHistory(cpCd);
    }
}
