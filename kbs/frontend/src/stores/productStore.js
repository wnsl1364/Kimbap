// /stores/productStore.js
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getFactoryList, getProdPlanList, postProdPlanListByCondition } from '@/api/production';

export const useProductStore = defineStore('product', () => {
  const factoryList = ref([]); // 공장 목록
  const prodPlanList = ref([]); // 생산계획 목록
  const condProdPlanList = ref([]); // 조건 생산계획 목록

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
    
  return {
    factoryList,
    prodPlanList,
    condProdPlanList,
    fetchFactoryList,
    fetchProdPlanList,
    fetchProdPlanListByCondition
  }
})