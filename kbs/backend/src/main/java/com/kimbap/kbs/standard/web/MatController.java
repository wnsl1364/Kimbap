package com.kimbap.kbs.standard.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kimbap.kbs.standard.service.MatService;
import com.kimbap.kbs.standard.service.MatVO;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/std/mat")
public class MatController {

    @Autowired
    private MatService matService;

    // 자재 목록 조회
    @GetMapping("/list")
    public List<MatVO> getMatList() {
        return matService.getMatList();
    }

    // 자재 등록 (공급사 포함)
    @PostMapping("/register")
    public void registerMat(@RequestBody MatVO mat) {
        matService.insertMatWithSuppliers(mat);
    }
}