<script setup>
import { ref, onMounted, computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useProductStore } from '@/stores/productStore'
import SearchForm from '@/components/kimbap/searchform/SearchForm.vue'
import StandartTable from '@/components/kimbap/table/StandardTable.vue'

const store = useProductStore()
const { factoryList, prodPlanList } = storeToRefs(store)
const { fetchFactoryList, fetchProdPlanList } = store

// 공장 목록 조회
onMounted(async () => {
  await fetchFactoryList()
  await fetchProdPlanList()
  console.log(prodPlanList.value)
})

console.log(prodPlanList.value)
// 공장 드롭다운 옵션
const factoryOptions = computed(() => [
  { label: '전체', value: '' },  
  ...factoryList.value.map(f => ({
    label: f.facName,
    value: f.fcode
  }))
])

// 검색 조건 정의
const searchColumns = [
  { key: 'produPlanCd', label: '생산계획번호', type: 'text', placeholder: '예: PLN-20250716-1' },
  { key: 'planDtRange', label: '계획일자', type: 'dateRange', startPlaceholder: '시작일', endPlaceholder: '종료일' },
  { key: 'planRange', label: '계획기간', type: 'dateRange', startPlaceholder: '시작일', endPlaceholder: '종료일' },
  {
    key: 'factory',
    label: '공장',
    type: 'dropdown',
    options: factoryOptions,
    placeholder: '공장을 선택하세요'
  },
];
const tableColumns = [
  { field: 'produPlanCd', header: '생산계획번호' },
  { field: 'planDt', header: '계획일자' },
  { field: 'planStartDt', header: '계획기간(시작)' },
  { field: 'planEndDt', header: '계획기간(종료)' },
  { field: 'facName', header: '공장' },
  { field: 'totalQty', header: '총생산수량' },
  { field: 'unit', header: '단위' },
  { field: 'note', header: '비고' }
]
</script>
<template>
  <div>
    <!-- 검색 모달 -->
    <SearchForm :columns="searchColumns" @search="handleSearch" @reset="handleReset" />


    <!-- 여기에 검색 결과 테이블 표시 -->
    <StandartTable
      :title="'생산계획 목록'"
      :data="planData"
      :columns="tableColumns"
      dataKey="produPlanCd"
      scrollHeight="55vh"
      :showHistoryButton="false"
    />
  </div>
</template>