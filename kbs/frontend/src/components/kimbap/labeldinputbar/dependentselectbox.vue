<template>
  <div class="flex items-center gap-4">
    <!-- 1단계: 주체 선택 -->
    <Dropdown
      v-model="selectedType"
      :options="typeOptions"
      optionLabel="label"
      optionValue="value"
      placeholder="구분 선택"
      class="w-48"
    />

    <!-- 2단계: 항목 선택 -->
    <Dropdown
      v-model="selectedItem"
      :options="filteredItems"
      optionLabel="label"
      optionValue="value"
      placeholder="목록 선택"
      class="w-48"
      :disabled="!selectedType"
    />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import Dropdown from 'primevue/dropdown'

// 1단계: 공급업체 / 매출업체
const typeOptions = [
  { label: '공급업체', value: 'supplier' },
  { label: '매출업체', value: 'customer' }
]

const selectedType = ref(null)
const selectedItem = ref(null)

// 2단계: 항목들 (분리해둠)
const supplierList = [
  { label: '공급업체 A', value: 'supplierA' },
  { label: '공급업체 B', value: 'supplierB' }
]

const customerList = [
  { label: '매출업체 X', value: 'customerX' },
  { label: '매출업체 Y', value: 'customerY' }
]

// 선택된 유형에 따라 목록 필터링
const filteredItems = computed(() => {
  if (selectedType.value === 'supplier') return supplierList
  if (selectedType.value === 'customer') return customerList
  return []
})
</script>
