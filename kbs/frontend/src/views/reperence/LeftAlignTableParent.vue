<script setup>
import { ref, onMounted } from 'vue'
/*import axios from 'axios'*/
import LeftAlignTable from '@/components/kimbap/table/LeftAlignTable.vue'

// Pinia store
import { storeToRefs } from 'pinia'; // storeToRefs를 사용해야만 반응형이 유지됨
import { useOrderFormStore } from '@/stores/orderFormStore' //피니아 스토어 가져오기

const store = useOrderFormStore()
const { formData } = storeToRefs(store)

const formFields = [
    { label: '주문번호', field: 'orderNo', type: 'text', disabled: true },
    { label: '주문일자', field: 'orderDate', type: 'text', disabled: true  },
    { label: '거래처명', field: 'customerName', type: 'input', readonly: true, suffixIcon: 'pi pi-search', suffixEvent: 'openQtyModal' },
    { label: '배송지주소', field: 'address', type: 'text', readonly: true },
    { label: '납기요청일자', field: 'dueDate', type: 'calendar', readonly: true },
    { label: '입금예정일자', field: 'paymentDate', type: 'calendar', readonly: true },
    { label: '비고', field: 'memo', type: 'text' },
    { label: '미수금', field: 'unpaid', type: 'text', readonly: true, suffixButton: '미수금 조회', align: 'right' }
]
onMounted(async () => {
    // 임시 데이터 예시
  store.setFormData({
    orderNo: 'ORD-20250722-001',
    orderDate: '2025-07-22',
    customerName: '김밥마트',
    address: '서울시 김밥구',
    dueDate: '2025-07-25',
    paymentDate: '2025-07-27',
    memo: '오전 납품',
    unpaid: '150,000'
  })
})

/** 
  * db로 가져올때
onMounted(async () => {
    const res = await axios.get('/api/orders/1')   // 예시 URL
    formData.value = res.data // 받아온 데이터를 formData에 바로 넣음
})
*/
</script>

<template>
    <div class="space-y-4">
        <LeftAlignTable :data="formData" :fields="formFields"/>
    </div>
</template>
