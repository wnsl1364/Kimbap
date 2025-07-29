import { difineStore } from 'pinia';
import { ref, computed } from 'vue';

export const useMatePutawayStore = defineStore('matePutaway', () => {
  const putawayData = ref([]);
  const putawayColumns = ref([]);
  
  // 입고 처리된 자재들을 저장할 필드 (히스토리 관리용)
  const processedPutawayMaterials = ref([]);

  // 입고 처리된 자재들을 추가하는 함수
  const addProcessedPutawayMaterials = (materials) => {
    processedPutawayMaterials.value.push(...materials);
  };

  return {
    putawayData,
    putawayColumns,
    processedPutawayMaterials,
    addProcessedPutawayMaterials
  };
});