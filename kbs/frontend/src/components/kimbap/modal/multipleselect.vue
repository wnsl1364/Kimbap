<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import Dialog from 'primevue/dialog'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import InputText from 'primevue/inputtext'
import InputNumber from 'primevue/inputnumber'
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

// 입력 값들을 추적하는 reactive 객체
const inputValues = ref({})

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
  // 선택된 아이템들에 입력값 추가
  const itemsWithInputs = selectedItems.value.map(item => {
    const itemId = item[props.itemKey]
    return {
      ...item,
      ...inputValues.value[itemId] // 입력된 값들 병합
    }
  })
  
  emit('update:modelValue', itemsWithInputs)
  emit('update:visible', false)
}

// 입력값 업데이트 함수
function updateInputValue(itemId, field, value) {
  if (!inputValues.value[itemId]) {
    inputValues.value[itemId] = {}
  }
  inputValues.value[itemId][field] = value
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
      scrollable
      scrollHeight="384px"
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
        :headerClass="col.align === 'right' ? 'text-right' : col.align === 'center' ? 'text-center' : ''"
        :bodyClass="col.align === 'right' ? 'text-right' : col.align === 'center' ? 'text-center' : ''"
      >
        <template #body="slotProps" v-if="col.type === 'input'">
          <InputNumber
            v-if="col.inputType === 'number'"
            :modelValue="inputValues[slotProps.data[itemKey]]?.[col.field] || ''"
            @update:modelValue="updateInputValue(slotProps.data[itemKey], col.field, $event)"
            :placeholder="col.placeholder || ''"
            :min="0"
            :max="col.field === 'moveQty' ? slotProps.data.qty : undefined"
            class="w-full"
            size="small"
            :step="1"
          />
          <InputText
            v-else
            :modelValue="inputValues[slotProps.data[itemKey]]?.[col.field] || ''"
            @update:modelValue="updateInputValue(slotProps.data[itemKey], col.field, $event)"
            :placeholder="col.placeholder || ''"
            class="w-full"
            size="small"
          />
        </template>
        <template #body="slotProps" v-else>
          {{ slotProps.data[col.field] }}
        </template>
      </Column>
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




