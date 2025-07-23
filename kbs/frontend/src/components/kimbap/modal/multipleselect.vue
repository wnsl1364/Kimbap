<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import Dialog from 'primevue/dialog'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import InputText from 'primevue/inputtext'
import Button from 'primevue/button'

const props = defineProps({
  visible: Boolean,
  items: { type: Array, default: () => [] }, // 전체 데이터
  modelValue: { type: Array, default: () => [] }, // 선택된 항목들
  itemKey: { type: String, default: 'id' }, // 고유키 필드명
  columns: { type: Array, required: true }, // 보여줄 컬럼 목록
  fetchItems: Function, // 선택적으로 fetch할 함수
})

const emit = defineEmits(['update:visible', 'update:modelValue', 'update:items'])

const searchText = ref('')
const selectedItems = ref([...props.modelValue])

watch(
  () => props.modelValue,
  (val) => {
    selectedItems.value = [...val]
  }
)

const filteredItems = computed(() => {
  if (!searchText.value) return props.items
  return props.items.filter((item) =>
    Object.values(item).some((val) =>
      String(val).toLowerCase().includes(searchText.value.toLowerCase())
    )
  )
})

onMounted(async () => {
  if (props.fetchItems) {
    const data = await props.fetchItems()
    emit('update:items', data)
  }
})

function onClose() {
  emit('update:visible', false)
}

function onConfirm() {
  emit('update:modelValue', selectedItems.value)
  emit('update:visible', false)
}
</script>
<template>
  <Dialog
    :visible="visible"
    @update:visible="emit('update:visible', $event)"
    modal
    header="항목 선택"
    :style="{ width: '60rem' }"
    :closable="false"
  >
    <!-- 🔍 검색창 -->
    <div class="mb-4">
      <InputText
        v-model="searchText"
        placeholder="검색어를 입력하세요"
        class="w-full"
      />
    </div>

    <!-- 📋 테이블 (다중 선택) -->
    <DataTable
      :value="filteredItems"
      v-model:selection="selectedItems"
      selectionMode="multiple"
      :dataKey="itemKey"
      tableStyle="min-width: 50rem"
      showGridlines
    >
      <Column
        selectionMode="multiple"
        headerClass="bg-gray-100 text-center"
        bodyClass="text-center"
        headerStyle="width: 3rem"
      />

      <Column
        v-for="col in columns"
        :key="col.field"
        :field="col.field"
        :header="col.header"
      />
    </DataTable>

    <!-- 하단 버튼 -->
    <div class="flex justify-end gap-2 mt-4">
      <Button label="닫기" severity="secondary" @click="onClose" />
      <Button label="확인" @click="onConfirm" :disabled="!selectedItems.length" />
    </div>
  </Dialog>


    <!-- 사용예시 
     <MultiSelectDialog
      v-model:visible="dialogVisible"
      v-model:modelValue="selectedProducts"
      :items="products"
      :itemKey="'code'"
      :columns="[
        { field: 'code', header: 'Code' },
        { field: 'name', header: 'Name' },
        { field: 'category', header: 'Category' },
        { field: 'quantity', header: 'Quantity' }
      ]"
    />
     -->
</template>




