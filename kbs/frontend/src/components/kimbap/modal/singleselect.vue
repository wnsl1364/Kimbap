<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import Dialog from 'primevue/dialog'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import InputText from 'primevue/inputtext'
import Button from 'primevue/button'

const props = defineProps({
  visible: Boolean,
  items: Array, // 전달받은 항목 리스트
  itemKey: { type: String, default: 'id' }, // 고유 키 필드명
  columns: Array, // [{ field: 'code', header: 'Code' }, ...]
  modelValue: Object, // 선택된 항목
  fetchItems: Function, // 선택적으로 비동기 항목 로드
})

const emit = defineEmits(['update:visible', 'update:modelValue'])

const searchText = ref('')
const filteredItems = computed(() => {
  if (!searchText.value) return props.items
  return props.items.filter((item) =>
    Object.values(item).some((val) =>
      String(val).toLowerCase().includes(searchText.value.toLowerCase())
    )
  )
})

const selectedItem = ref(props.modelValue)

watch(
  () => props.modelValue,
  (val) => {
    selectedItem.value = val
  }
)

onMounted(async () => {
  if (props.fetchItems) {
    const res = await props.fetchItems()
    emit('update:items', res)
  }
})

function onClose() {
  emit('update:visible', false)
}

function onConfirm() {
  emit('update:modelValue', selectedItem.value)
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
    <!-- 🔍 검색 -->
    <div class="mb-4">
      <InputText
        v-model="searchText"
        placeholder="검색어를 입력하세요"
        class="w-full"
      />
    </div>

    <!-- 📋 테이블 -->
    <DataTable
      :value="filteredItems"
      v-model:selection="selectedItem"
      selectionMode="single"
      :dataKey="itemKey"
      tableStyle="min-width: 50rem"
      showGridlines
      scrollable
      scrollHeight="384px"
    >
      <Column selectionMode="single" headerStyle="width: 3rem" />

      <Column
        v-for="col in columns"
        :key="col.field"
        :field="col.field"
        :header="col.header"
      />
    </DataTable>

    <!-- 버튼 -->
    <div class="flex justify-end gap-2 mt-4">
      <Button label="닫기" severity="secondary" @click="onClose" />
      <Button label="확인" @click="onConfirm" :disabled="!selectedItem" />
    </div>
  </Dialog>
  <!-- 사용예시 
   <ProductSelectDialog
      v-model:visible="dialogVisible"
      v-model:modelValue="selectedProduct"
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