// /stores/productStore.js
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getFactoryList, getProdPlanList } from '@/api/production';

export const useProductStore = defineStore('product', () => {
  const factoryList = ref([]); // 공장 목록
  const prodPlanList = ref([]); // 생산계획 목록

  // 공장 목록 조회
  const fetchFactoryList = async () => {
        try {
          const res = await getFactoryList();
          factoryList.value = res.data;
        } catch (err) {
          console.error('공장 목록 조회 실패:', err);
        }
  }

  // 공장 목록 조회
  const fetchProdPlanList = async () => {
        try {
          const res = await getProdPlanList();
          prodPlanList.value = res.data;
        } catch (err) {
          console.error('생산계획 목록 조회 실패:', err);
        }
  }
  return {
    factoryList,
    prodPlanList,
    fetchFactoryList,
    fetchProdPlanList
  }
})