import { defineStore } from 'pinia';
import { ref } from 'vue';
import { format, isValid } from 'date-fns';
import { getMaterialFlowList } from '@/api/materials';
import { useToast } from 'primevue/usetoast';

export const usemathistoryListStore = defineStore('MatHistory', () => {
  const mathistoryList = ref([]); // 입출고 내역 목록
  const toast = useToast();

  // 자재 입출고 내역 조회
  const fetchMatHistorys = async (searchData = {}) => {
    const dateRange = searchData.regDt || {};

    // ✅ 필드명 백엔드 VO에 맞게 수정 (regDtStart, regDtEnd)
    const params = {
      movementType: searchData.movementType || '',
      regDtStart: isValid(dateRange.start) ? format(dateRange.start, 'yyyy-MM-dd') : '',
      regDtEnd: isValid(dateRange.end) ? format(dateRange.end, 'yyyy-MM-dd') : '',
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
    fetchMatHistorys
  };
});