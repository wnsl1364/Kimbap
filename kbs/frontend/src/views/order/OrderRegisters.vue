<script setup>
import { ref, onMounted } from 'vue'
import BasicTableCheckOne from '@/components/kimbap/table/BasicTableCheckOne.vue'
import { ProductService } from '@/service/ProductService'

const products = ref([])
const productColumns = [
    { field: 'code', header: '코드' },
    { field: 'name', header: 'Name' },
    { field: 'category', header: 'Category' },
    { field: 'quantity', header: 'Quantity' }
]

onMounted(() => {
    ProductService.getProductsMini().then((data) => products.value = data)
})

/** db로 가져올때
 * 
 * const columns = [
    { field: 'code', header: '제품코드' },
    { field: 'name', header: '제품명' },
    { field: 'category', header: '카테고리' },
    { field: 'quantity', header: '수량' }
]

const products = ref([])

onMounted(async () => {
    const response = await axios.get('/api/products')
    products.value = response.data
})

 */
</script>
<template>
  <div class="space-y-4">
    <BasicTableCheckOne :data="products" :columns="productColumns" title="타이틀 맘대로"/>
  </div>
</template>
