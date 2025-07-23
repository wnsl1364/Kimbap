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

const productList = ref([
  {
    id: 1,
    prod_code: 'KMP0123456',
    prod_name: '불고기 김밥',
    prod_qty: '100.00',
    unit: 'EA',
    prod_date: '',
    priority: '1'
  },
  {
    id: 2,
    prod_code: '',
    prod_name: '',
    prod_qty: '',
    unit: '',
    prod_date: '',
    priority: ''
  }
])

const selectedRows = ref([])

const productColumns = [
  { field: 'prod_code', header: '제품코드', type: 'input', align: 'left' },
  { field: 'prod_name', header: '제품명', type: 'input', align: 'left' },
  { field: 'prod_qty', header: '생산수량', type: 'input', align: 'right' },
  { field: 'unit', header: '단위', type: 'input', align: 'center' },
  { field: 'prod_date', header: '생산예정일자', type: 'input', inputType: 'date', align: 'center' },
  { field: 'priority', header: '우선순위', type: 'input', align: 'center' }
]
/** db로 가져올때
onMounted(async () => {
  const response = await axios.get('/api/products')
  store.setProducts(response.data)
})
 */
const formData = ref({
  plan_no: '',
  plan_date: '',
  plan_period: '2025-07-15 ~ 2025-07-16',
  factory: '',
  manager: '',
  note: ''
})

const fields = [
  { field: 'plan_no', label: '생산계획번호', type: 'input', readonly: true },
  { field: 'plan_date', label: '계획일자', type: 'input', inputType: 'date' },
  { field: 'plan_period', label: '계획기간', type: 'input' },
  { field: 'manager', label: '담당자', type: 'input' },
  { field: 'factory', label: '공장', type: 'input' },
  { field: 'note', label: '비고', type: 'input' },
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
  <div class="space-y-8">
    <!-- 생산계획 기본 정보 -->
    <LeftAlignTable
      v-model:data="formData"
      :fields="fields"
      title="생산계획 기본 정보"
      :buttons="{
        save: { show: true, label: '저장', severity: 'success' },
        reset: { show: true, label: '초기화', severity: 'secondary' },
        delete: { show: true, label: '삭제', severity: 'danger' },
        load: { show: true, label: '생산계획 불러오기', severity: 'info' }
      }"
      buttonPosition="top"
    >

    </LeftAlignTable>

    <!-- 제품 목록 -->
    <div>
      <div class="flex justify-between items-center mb-2">
        <h2 class="text-md font-semibold">제품</h2>
        <div class="space-x-2">
          <Button label="제품삭제" severity="danger" />
          <Button label="제품추가" severity="success" />
        </div>
      </div>
      <BasicTable
        v-model:selection="selectedRows"
        :data="productList"
        :columns="productColumns"
        :selectionMode="'multiple'"
        dataKey="id"
        scrollHeight="300px"
      />
    </div>
  </div>
</template>