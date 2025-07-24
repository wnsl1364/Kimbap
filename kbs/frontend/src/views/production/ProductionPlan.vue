<script setup>
import { ref, onMounted, computed } from 'vue'
import InputForm from '@/components/kimbap/searchform/inputForm.vue'
import InputTable from '@/components/kimbap/table/InputTable.vue';
import { storeToRefs } from 'pinia'
import { useProductStore } from '@/stores/productStore'

const store = useProductStore()
const { factoryList } = storeToRefs(store)
const { fetchFactoryList } = store

// 공장 목록 조회
onMounted(async () => {
  await fetchFactoryList()
})

// 공장 드롭다운 옵션
const factoryOptions = computed(() =>
  factoryList.value.map(f => ({
    label: f.facName,
    value: f.fcode
  }))
)

// 폼 필드 정의 (InputForm.vue 기준 key 속성 사용)
const fields = [
  { key: 'plan_no', label: '생산계획번호', type: 'readonly' },
  { key: 'plan_date', label: '계획일자', type: 'text', placeholder: 'YYYY-MM-DD' },
  { key: 'plan_period', label: '계획기간', type: 'text', placeholder: '2025-07-15 ~ 2025-07-16' },
  { key: 'manager', label: '담당자', type: 'text' },
  {
    key: 'factory',
    label: '공장',
    type: 'dropdown2',
    options: factoryOptions,
    placeholder: '공장을 선택하세요'
  },
  { key: 'note', label: '비고', type: 'textarea' }
]

const prodPlanFormButtons = ref({
  save: { show: true, label: '저장', severity: 'success' },
  reset: { show: true, label: '초기화', severity: 'secondary' },
  delete: { show: true, label: '삭제', severity: 'danger' },
  load: { show: true, label: '생산계획 불러오기', severity: 'info' }
})

// 제품 목록 데이터
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

// 선택된 행
const selectedRows = ref([])

// 제품 테이블 컬럼 정의
const productColumns = [
  { field: 'prod_code', header: '제품코드', type: 'input', align: 'left' },
  { field: 'prod_name', header: '제품명', type: 'input', align: 'left' },
  { field: 'prod_qty', header: '생산수량', type: 'input', align: 'right' },
  { field: 'unit', header: '단위', type: 'input', align: 'center' },
  { field: 'prod_date', header: '생산예정일자', type: 'input', inputType: 'date', align: 'center' },
  { field: 'priority', header: '우선순위', type: 'input', align: 'center' }
]

// 버튼 이벤트 핸들러
const handleSave = (data) => {
  console.log('✅ 저장 데이터:', data)
}

const handleReset = () => {
  console.log('🧼 폼 초기화됨')
}

const handleDelete = (data) => {
  console.log('🗑 삭제 요청됨:', data)
}

const handleLoad = () => {
  console.log('📦 불러오기 요청')
}
</script>

<template>
  <div class="space-y-8">
    <!-- 생산계획 입력 폼 -->
    <InputForm
      :columns="fields"
      title="생산계획 기본 정보"
      :buttons="prodPlanFormButtons"
      buttonPosition="top"
      @submit="handleSave"
      @reset="handleReset"
      @delete="handleDelete"
      @load="handleLoad"
    />
    <!-- 제품 목록 테이블 -->
    <div>
      <InputTable
        v-model:data="productList"
        :columns="productColumns"
        :title="'제품 목록'"
        :dataKey="'id'"
        buttonPosition="top"
        enableRowActions
        enableSelection
        scrollHeight="300px"
      />
    </div>
  </div>
</template>