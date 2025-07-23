// stores/orderProductStore.js
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useOrderProductStore = defineStore('orderProduct', () => {
  const products = ref([])

  const setProducts = (data) => {
    products.value = data
  }

  const resetProducts = () => {
    products.value = []
  }

  return {
    products,
    setProducts,
    resetProducts
  }
})
