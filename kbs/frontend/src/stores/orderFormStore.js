// stores/orderFormStore.js
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useOrderFormStore = defineStore('orderForm', () => {
  const formData = ref({
    orderNo: '',
    orderDate: '',
    customerName: '',
    address: '',
    dueDate: '',
    paymentDate: '',
    memo: '',
    unpaid: ''
  })

  const setFormData = (data) => {
    formData.value = data
  }

  const resetForm = () => {
    formData.value = {
      orderNo: '',
      orderDate: '',
      customerName: '',
      address: '',
      dueDate: '',
      paymentDate: '',
      memo: '',
      unpaid: ''
    }
  }

  return {
    formData,
    setFormData,
    resetForm
  }
})
