<script setup>
import { ref, onMounted } from 'vue'
/*import axios from 'axios'*/
import InputTable from '@/components/kimbap/table/InputTable.vue'

const products = ref([
    {
        code: 'P001',
        name: '김밥',
        qty: '',
        price: 7000,
        dueDate: ''
    },
    {
        code: 'P002',
        name: '냉동김밥',
        qty: '',
        price: 5000,
        dueDate: ''
    },
    {
        code: 'P003',
        name: '참치김밥',
        qty: '',
        price: 8000,
        dueDate: ''
    },
    {
        code: 'P004',
        name: '야채김밥',
        qty: '',
        price: 6000,
        dueDate: ''
    }
])

const columns = [
    { field: 'code', header: '제품코드', type: 'readonly' },
    { field: 'name', header: '제품명', type: 'readonly' },
    { field: 'qty', header: '주문수량', type: 'input', inputType: 'number' },
    { field: 'price', header: '단가', type: 'readonly' },
    { field: 'dueDate', header: '납기일자', type: 'calendar' }
]


/**
 * db로 가져올때
 * const products = ref([])

const columns = [
    { field: 'code', header: '제품코드', type: 'readonly' },
    { field: 'name', header: '제품명', type: 'readonly' },
    { field: 'qty', header: '주문수량', type: 'input', inputType: 'number' },
    { field: 'price', header: '단가', type: 'readonly' },
    { field: 'dueDate', header: '납기일자', type: 'input', inputType: 'date' }
]

onMounted(async () => {
    const res = await axios.get('/api/orders/1')
    products.value = res.data.products
})

const save = async () => {
    await axios.post('/api/orders/1/save', { products: products.value })
    alert('저장 완료')
}
 */
</script>
<template>
    <div class="space-y-4">
        <InputTable :data="products" :columns="columns" title="타이틀 맘대로"/>
        <button @click="save">저장</button>
    </div>
</template>