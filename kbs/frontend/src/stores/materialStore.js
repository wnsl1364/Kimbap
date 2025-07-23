import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useMaterialStore = defineStore('material', () => {
  const materials = ref([]);

  const setMaterials = (newMaterials) => {
    materials.value = newMaterials;
  };
  const searchColumns = ref([]);
  const purchaseColumns = ref([]);
  const purchaseData = ref([]);
//------------------------------------------------------------------------------------------------------------------------
  const inboundData = ref({});
  const inMaterialColumns = ref([]);
  const warehouseColumns = ref([]);
  const inboundFields = ref([]);
  // 선택된 자재들을 저장할 새로운 필드 추가
  const selectedInboundMaterials = ref([]);
  
  // 선택된 자재들을 설정하는 함수
  const setSelectedInboundMaterials = (materials) => {
    selectedInboundMaterials.value = materials;
  };
  
  // 입고 처리된 자재들을 저장할 필드 (히스토리 관리용)
  const processedInboundMaterials = ref([]);
  
  // 입고 처리된 자재들을 추가하는 함수
  const addProcessedInboundMaterials = (materials) => {
    processedInboundMaterials.value.push(...materials);
  };


  return {
    materials,
    setMaterials,
    searchColumns,
    purchaseColumns,
    purchaseData,
    inboundData,
    inMaterialColumns,
    warehouseColumns,
    inboundFields,
    selectedInboundMaterials,
    setSelectedInboundMaterials,
    processedInboundMaterials,
    addProcessedInboundMaterials
  };
});