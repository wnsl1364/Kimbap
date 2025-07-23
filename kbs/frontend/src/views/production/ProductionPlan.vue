<script setup>
import { ref, onMounted } from 'vue'
import LeftAlignTable from '@/components/kimbap/table/LeftAlignTable.vue'
// import axios from 'axios'
import BasicTable from '@/components/kimbap/table/BasicTable.vue'
import { ProductService } from '@/service/ProductService'

// Pinia store
import { storeToRefs } from 'pinia'; // storeToRefs를 사용해야만 반응형이 유지됨
import { useProductStore } from '@/stores/productStore' //피니아 스토어 가져오기

const store = useProductStore()
const { products } = storeToRefs(store)

const productColumns = [
    { field: 'code', header: '코드'},
    { field: 'name', header: '이름', type: 'input', readonly: true},
    { field: 'proName', header: '제품명', type: 'input', suffixIcon: 'pi pi-search', suffixEvent: 'openQtyModal',  },
    { field: 'category', header: '카테고리' },
    { field: 'quantity', header: '수량', type: 'input', inputType: 'number', align: 'right'}
]

onMounted(() => {
  ProductService.getProductsMini().then(data => {
    store.setProducts(data)
  })
})

/** db로 가져올때
onMounted(async () => {
  const response = await axios.get('/api/products')
  store.setProducts(response.data)
})
 */
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
    { label: '생산계획번호', field: 'orderNo', type: 'text', disabled: true },
    { label: '계획일자', field: 'orderDate', type: 'text', disabled: true  },
    { label: '계획기간', field: 'customerName', type: 'input', readonly: true, suffixIcon: 'pi pi-search', suffixEvent: 'openQtyModal' },
    { label: '담당자', field: 'address', type: 'text', readonly: true },
    { label: '공장', field: 'dueDate', type: 'calendar', readonly: true },
    { label: '비고', field: 'paymentDate', type: 'calendar', readonly: true },]

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
      <LeftAlignTable :data="formData" :fields="formFields"/>
  </div>
  <div class="space-y-4">
    <BasicTable :data="products" :columns="productColumns"/>
  </div>
</template>