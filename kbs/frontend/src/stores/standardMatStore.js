// stores/standardMatStore.js
import { defineStore } from 'pinia';
import { ref } from 'vue';
import { getMaterialList, insertMaterial, getSupplierList } from '@/api/standard';

export const useStandardMatStore = defineStore('standardMat', () => {
  // 전역 데이터 상태만
  const materialList     = ref([]);    // 자재 목록
  const selectedMaterial = ref(null);  // 선택된 자재
  const supplierList     = ref([]);    // 공급처 목록
  const searchFilter     = ref({});    // 검색 필터

  // 비동기 액션
  const fetchMaterials = async () => {
    try {
      const res = await getMaterialList();
      materialList.value = res.data;
    } catch (err) {
      console.error('자재 목록 조회 실패:', err);
    }
  };

  const addMaterial = async (data) => {
    try {
      const res = await insertMaterial(data);
      if (res.data?.success) {
        await fetchMaterials();
      }
      return res.data;
    } catch (err) {
      console.error('자재 등록 실패:', err);
      throw err;
    }
  };

  const fetchSuppliers = async () => {
    try {
      const res = await getSupplierList();
      supplierList.value = res.data;
    } catch (err) {
      console.error('공급처 조회 실패:', err);
    }
  };

  const setSearchFilter = (filter) => {
    searchFilter.value = filter;
  };

  const selectMaterial = (row) => {
    selectedMaterial.value = row;
  };
  

  return {
    materialList,
    selectedMaterial,
    supplierList,
    searchFilter,
    fetchMaterials,
    fetchSuppliers,
    addMaterial,
    setSearchFilter,
    selectMaterial
  };
});
