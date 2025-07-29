// /stores/productStore.js
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getFactoryList, // 공장 목록 조회
         getProdPlanList, // 생산계획 목로고 조회
         postProdPlanListByCondition, // 생산계획 목록 조건 검색
         getProdPlanDetailList, // 생산계획별 상세내역 조회
         getAllProducts, // 제품정보 전부 조회
         postSaveProdPlan // 생산계획 등록
        } from '@/api/production';

export const useProductStore = defineStore('product', () => {
  const factoryList = ref([]); // 공장 목록
  const prodPlanList = ref([]); // 생산계획 목록
  const condProdPlanList = ref([]); // 조건 생산계획 목록
  const prodPlanDetailList = ref([]); // 생산계획 상세 목록
  const productList = ref([]); // 제품정보 목록

  // 공장 목록 조회
  const fetchFactoryList = async () => {
        try {
          const res = await getFactoryList();
          factoryList.value = res.data;
        } catch (err) {
          console.error('공장 목록 조회 실패:', err);
        }
  }

  // 생산계획 목록 조회
  const fetchProdPlanList = async () => {
        try {
          const res = await getProdPlanList();
          prodPlanList.value = res.data;
        } catch (err) {
          console.error('생산계획 목록 조회 실패:', err);
        }
  }
  // 조건 기반 생산계획 목록 조회
  const fetchProdPlanListByCondition = async (condition) => {
    try {
      const res = await postProdPlanListByCondition(condition);
      condProdPlanList.value = res.data;
    } catch (err) {
      console.error('조건 검색 실패:', err);
    }
  };
  // 생산계획의 상세 목록 조회
  const fetchProdPlanDetailList = async (produPlanCd) => {
    try {
      const res = await getProdPlanDetailList(produPlanCd);
      prodPlanDetailList.value = res.data;
    } catch (err) {
      console.error('생산계획상세 조회 실패:', err);
    }
  };
  // 제품 목록 조회
  const fetchProductList = async () => {
        try {
          const res = await getAllProducts();
          productList.value = res.data;
        } catch (err) {
          console.error('제품 목록 조회 실패:', err);
        }
  };
  // 생산계획 저장
  const saveProdPlan = async (planData) => {
    try {
      const res = await postSaveProdPlan(planData);
      return res.data;
    } catch (err) {
      console.error('생산계획 저장 실패:', err);
      throw err;
    }
  }

  return {
    factoryList,                    // 공장 목록
    prodPlanList,                   // 생산계획 목록
    condProdPlanList,               // 조건별 생산계획 목록
    prodPlanDetailList,             // 생산계획별 상세 목록
    productList,                    // 제품정보 목록
    fetchFactoryList,               // 공장 목록 조회 함수
    fetchProdPlanList,              // 생산계획 목록 조회 함수
    fetchProdPlanListByCondition,   // 생산계획 목록 조건 조회 함수
    fetchProdPlanDetailList,        // 생산계획의 상세 목록 조회
    fetchProductList,               // 제품정보 목록 조회
    saveProdPlan                    // 생산계획 저장
  }
})