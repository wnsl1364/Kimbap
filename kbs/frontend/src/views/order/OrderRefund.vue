<script setup>
import { ref, onMounted } from 'vue'
/*import axios from 'axios'*/
import LeftAlignTable from '@/components/kimbap/table/LeftAlignTable.vue'

const formData = ref({
    orderNo: '자동생성',
    orderDate: '2025-07-22',
    customerName: '거래처명',
    address: '',
    dueDate: '',
    paymentDate: '',
    memo: ''
})

const formFields = [
    { label: '주문번호', field: 'orderNo', type: 'text', readonly: true },
    { label: '주문일자', field: 'orderDate', type: 'text', readonly: true },
    { label: '거래처명', field: 'customerName', type: 'text', readonly: true },
    { label: '배송지주소', field: 'address', type: 'text', readonly: true },
    { label: '납기요청일자', field: 'dueDate', type: 'date', readonly: true },
    { label: '입금예정일자', field: 'paymentDate', type: 'date', readonly: true },
    { label: '비고', field: 'memo', type: 'text' },
    { label: '미수금', field: 'unpaid', type: 'text', readonly: true, suffixButton: '미수금 조회' }
]

/** 
  * db로 가져올때
  * const formData = ref({
      orderNo: '',
      orderDate: '',
      customerName: '',
      address: '',
      dueDate: '',
      paymentDate: '',
      memo: '',
      unpaid: ''
    })
onMounted(async () => {
    const res = await axios.get('/api/orders/1')   // 예시 URL
    formData.value = res.data // 받아온 데이터를 formData에 바로 넣음
})
*/
</script>

<template>
    <div class="space-y-4">
        <LeftAlignTable v-model:data="formData" :fields="formFields" />
    </div>
</template>
