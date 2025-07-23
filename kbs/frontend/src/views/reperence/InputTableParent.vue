<script setup>
import { ref, onMounted } from 'vue'
// import axios from 'axios'
import InputTable from '@/components/kimbap/table/InputTable.vue'

// Pinia store
import { storeToRefs } from 'pinia'; // storeToRefs를 사용해야만 반응형이 유지됨
import { useOrderProductStore } from '@/stores/orderProductStore' //피니아 스토어 가져오기

const store = useOrderProductStore()
const { products } = storeToRefs(store)

const columns = [
  { field: 'code', header: '제품코드', type: 'readonly' },
  { field: 'name', header: '제품명', type: 'input', readonly: true, suffixIcon: 'pi pi-search', suffixEvent: 'openQtyModal' },
  { field: 'qty', header: '주문수량', type: 'input', inputType: 'number', align: 'right' },
  { field: 'price', header: '단가', type: 'readonly', align: 'right' },
  { field: 'dueDate', header: '납기일자', type: 'calendar' }
]

// 임시 데이터 예시
onMounted(() => {
  store.setProducts([
    { code: 'P001', name: '김밥', qty: '', price: 7000, dueDate: '' },
    { code: 'P002', name: '냉동김밥', qty: '', price: 5000, dueDate: '' },
    { code: 'P003', name: '참치김밥', qty: '', price: 8000, dueDate: '' },
    { code: 'P004', name: '야채김밥', qty: '', price: 6000, dueDate: '' }
  ])
})

/**
 * db로 가져올때
onMounted(async () => {
  try {
    const res = await axios.get('/api/orders/1')
    store.setProducts(res.data.products) // products는 배열이어야 함
  } catch (err) {
    console.error('데이터 불러오기 실패:', err)
  }
})
 */
</script>
<template>
    <div class="space-y-4">
        <div class="flex justify-between items-center mb-4">
            <h2 class="text-lg mb-0 font-semibold">타이틀</h2>

            <div class="flex flex-wrap gap-2">
                <Button label="삭제" severity="danger" />
                <Button label="초기화" severity="contrast" />
                <Button label="저장" severity="info" />
                <Button label="불러오기" severity="success" />
            </div>
        </div>
        <InputTable :data="products" :columns="columns"/>
    </div>
</template>