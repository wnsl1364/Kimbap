import { defineStore } from 'pinia';
import { ref } from 'vue';
import { format } from 'date-fns';
import { getMaterialFlowList, getTodayMaterialFlowList } from '@/api/materials';
import { useToast } from 'primevue/usetoast';

export const usemathistoryListStore = defineStore('MatHistory', () => {
  const mathistoryList = ref([]); // 입출고 내역 목록
  const toast = useToast();

    const fetchTodayMatHistorys = async () => {
    try {
      const res = await getTodayMaterialFlowList();
      mathistoryList.value = res.data || [];
    } catch (err) {
      console.error('❌ 오늘 입출고 조회 실패:', err);
    }
  };

  // 자재 입출고 내역 조회
  const fetchMatHistorys = async (searchData = {}) => {
    const params = {
      movementType: searchData.movementType || '',
      regDtStart: searchData.regDtStart || '',
      regDtEnd: searchData.regDtEnd || '',
      mateName: searchData.mateName || '',
      wareName: searchData.wareName || '',
      lotNo: searchData.lotNo || ''
    };

    try {
      const res = await getMaterialFlowList(params);
      mathistoryList.value = res.data || [];

      if (!mathistoryList.value.length) {
        toast.add({
          severity: 'info',
          summary: '검색 결과 없음',
          detail: '조건에 해당하는 입출고 내역이 없습니다.',
          life: 3000
        });
      }
    } catch (err) {
      console.error("❌ 입출고 목록 조회 실패:", err);
      toast.add({
        severity: 'error',
        summary: '조회 실패',
        detail: '입출고 내역 조회 중 오류가 발생했습니다.',
        life: 3000
      });
    }
  };

  return {
    mathistoryList,
    fetchTodayMatHistorys,
    fetchMatHistorys
  };
});