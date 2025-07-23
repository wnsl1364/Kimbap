// stores/orderFormStore.js
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useOrderFormStore = defineStore('orderList', () => {
  const searchColumns = ref([]);

  return {
    searchColumns
  }
})
