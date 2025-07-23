<script setup>
import { onMounted } from 'vue'
// import axios from 'axios'
import BasicTableCheckOne from '@/components/kimbap/table/BasicTableCheckOne.vue'
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
</script>
<template>
  <div class="space-y-4">
    <BasicTableCheckOne :data="products" :columns="productColumns" scrollHeight="200px"/>
  </div>
</template>
