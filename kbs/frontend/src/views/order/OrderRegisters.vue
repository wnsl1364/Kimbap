<script setup>
import { ref, onMounted } from 'vue'
/*import axios from 'axios'*/
import LeftAlignTable from '@/components/kimbap/table/LeftAlignTable.vue'
import InputTable from '@/components/kimbap/table/InputTable.vue';

// Pinia store
import { storeToRefs } from 'pinia'; // storeToRefs를 사용해야만 반응형이 유지됨
import { useOrderFormStore } from '@/stores/orderFormStore'
import { useOrderProductStore } from '@/stores/orderProductStore' //피니아 스토어 가져오기

// 스토어 인스턴스
const formStore = useOrderFormStore()
const productStore = useOrderProductStore()

// 반응형 상태
const { formData } = storeToRefs(formStore)
const { products } = storeToRefs(productStore)

// store 메서드
const { setFormData, resetForm } = formStore
const { setProducts, resetProducts } = productStore

const formFields = [
    { label: '주문번호', field: 'orderNo', type: 'text', disabled: true },
    { label: '주문일자', field: 'orde rDate', type: 'text'  },
    { label: '거래처명', field: 'customerName', type: 'input', disabled: true },
    { label: '배송지주소', field: 'address', type: 'text' },
    { label: '납기요청일자', field: 'dueDate', type: 'calendar', readonly: true },
    { label: '입금일자', field: 'paymentDate', type: 'calendar' },
    { label: '비고', field: 'memo', type: 'text' }
]

const columns = [
  { field: 'code', header: '제품코드', type: 'readonly' },
  { field: 'name', header: '제품명', type: 'input', readonly: true, suffixIcon: 'pi pi-search', suffixEvent: 'openQtyModal' },
  { field: 'qty', header: '주문수량', type: 'input', inputType: 'number', align: 'right' },
  { field: 'price', header: '단가', type: 'readonly', align: 'right' },
  { field: 'dueDate', header: '납기일자', type: 'calendar' }
]
onMounted(async () => {
    // 임시 데이터 예시
  formStore.setFormData({
    orderNo: '',
    orderDate: '',
    customerName: '',
    address: '',
    dueDate: '',
    paymentDate: '',
    memo: ''
  })

  productStore.setProducts([
    { code: 'P001', name: '김밥', qty: '', price: 7000, dueDate: '' },
    { code: 'P002', name: '냉동김밥', qty: '', price: 5000, dueDate: '' },
    { code: 'P003', name: '참치김밥', qty: '', price: 8000, dueDate: '' },
    { code: 'P004', name: '야채김밥', qty: '', price: 6000, dueDate: '' }
  ])
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
    <div class="space-y-4 mt-8">
        <InputTable :data="products" :columns="columns" :title="'제품'"/>
        <!-- 하단 합계 영역 -->
        <div class="flex justify-end items-center mt-4 px-4">
          <p class="text-base font-semibold text-gray-700 mr-2 mb-0">총 주문 총액</p>
          <p class="text-xl font-bold text-orange-500">
            49,000,000 <em class="text-sm font-normal not-italic text-black ml-1">원</em>
          </p>
        </div>

    </div>
</template>
