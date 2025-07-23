// stores/standardMatStore.js
import { defineStore } from 'pinia';
import { getMaterialList, insertMaterial, getSupplierList } from '@/api/standard'; // 자재 및 거래처 관련 API

export const useStandardMatStore = defineStore('standardMat', {
  // 🔸 상태 정의
  state: () => ({
    materialList: [],       // 자재 목록 데이터
    selectedMaterial: null, // 선택된 자재 정보 (예: 이력 조회 시 사용)
    supplierList: [],       // 거래처 목록 데이터 (공급처 등)
    searchFilter: {}        // 검색 조건 저장용 객체
  }),

  // 🔸 비동기 및 상태 변경 함수 정의
  actions: {
    // 자재 목록 조회 (조회 API 호출)
    async fetchMaterials() {
      const res = await getMaterialList();
      this.materialList = res.data;
    },

    // 자재 등록 처리 (등록 성공 시 자재 목록 재조회)
    async addMaterial(materialData) {
      const res = await insertMaterial(materialData);
      if (res.data?.success) {
        await this.fetchMaterials(); // 등록 후 목록 다시 불러오기
      }
      return res.data;
    },

    // 거래처(공급처) 목록 조회
    async fetchSuppliers() {
      const res = await getSupplierList(); // ✅ 함수명에 맞게 getCustomers 사용
      this.supplierList = res.data;
    },

    // 검색 조건 저장 (예: 검색 후 목록 필터링할 때 사용)
    setSearchFilter(payload) {
      this.searchFilter = payload;
    },

    // 선택된 자재 저장 (예: 이력 조회 등 클릭 시 사용)
    selectMaterial(rowData) {
      this.selectedMaterial = rowData;
    }
  }
});
