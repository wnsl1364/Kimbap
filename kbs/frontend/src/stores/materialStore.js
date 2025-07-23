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

  return {
    materials,
    setMaterials,
    searchColumns,
    purchaseColumns,
    purchaseData
  };
});