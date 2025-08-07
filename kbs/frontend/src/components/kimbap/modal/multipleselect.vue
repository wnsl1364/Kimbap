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
const selectedItems = ref([])

// 입력 값들을 추적하는 reactive 객체
const inputValues = ref({})
// 모달이 열릴 때의 초기 상태를 저장 (취소 시 복원용)
const initialInputValues = ref({})
const initialSelectedItems = ref([])

// props.items가 변경될 때마다 inputValues 동기화
watch(
  () => props.items,
  (newItems) => {
    if (newItems && newItems.length > 0) {
      const newInputValues = {}
      newItems.forEach(item => {
        const itemId = item[props.itemKey]
        // 기존 값을 사용하지 않고 항목의 실제 값 또는 0으로 설정
        const inputFields = {}
        props.columns.forEach(col => {
          if (col.type === 'input') {
            // 항목에 해당 필드값이 있으면 사용, 없으면 기본값 사용
            inputFields[col.field] = item[col.field] || (col.inputType === 'number' ? 0 : '')
          }
        })
        newInputValues[itemId] = inputFields
      })
      inputValues.value = newInputValues
    }
  },
  { immediate: true, deep: true }
)

// props.modelValue 변경 감지
watch(
  () => props.modelValue,
  (val) => {
    selectedItems.value = [...val]
  },
  { immediate: true }
)

// 모달 표시 상태 변경 감지
watch(
  () => props.visible,
  (newVisible, oldVisible) => {
    if (newVisible && !oldVisible) {
      // 모달이 열릴 때 - 입력값 완전 초기화 후 초기 상태 저장
      initializeInputValues()
      saveInitialState()
    }
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

// 아이템에서 입력 가능한 필드들만 추출
function extractInputFields(item) {
  const inputFields = {}
  props.columns.forEach(col => {
    if (col.type === 'input') {
      inputFields[col.field] = item[col.field] || (col.inputType === 'number' ? 0 : '')
    }
  })
  return inputFields
}

// 입력값 완전 초기화 함수 (모달이 열릴 때마다 실행)
function initializeInputValues() {
  const newInputValues = {}
  if (props.items && props.items.length > 0) {
    props.items.forEach(item => {
      const itemId = item[props.itemKey]
      // 모든 입력 필드를 0 또는 빈 문자열로 초기화
      const inputFields = {}
      props.columns.forEach(col => {
        if (col.type === 'input') {
          inputFields[col.field] = col.inputType === 'number' ? 0 : ''
        }
      })
      newInputValues[itemId] = inputFields
    })
  }
  inputValues.value = newInputValues
}

// 초기 상태 저장 (모달이 열릴 때)
function saveInitialState() {
  // 현재 선택된 아이템들 저장
  initialSelectedItems.value = [...selectedItems.value]
  
  // 현재 입력값들 저장 (deep copy)
  initialInputValues.value = {}
  Object.keys(inputValues.value).forEach(itemId => {
    initialInputValues.value[itemId] = { ...inputValues.value[itemId] }
  })
}

// 초기 상태로 복원 (취소 시)
function restoreInitialState() {
  // 선택된 아이템들 복원
  selectedItems.value = [...initialSelectedItems.value]
  
  // 입력값들 복원
  inputValues.value = {}
  Object.keys(initialInputValues.value).forEach(itemId => {
    inputValues.value[itemId] = { ...initialInputValues.value[itemId] }
  })
}

function onClose() {
  // 취소 시 초기 상태로 복원
  restoreInitialState()
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

// 현재 아이템의 입력값 가져오기
function getInputValue(itemId, field) {
  return inputValues.value[itemId]?.[field] || ''
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
            :modelValue="getInputValue(slotProps.data[itemKey], col.field)"
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
            :modelValue="getInputValue(slotProps.data[itemKey], col.field)"
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
      <Button label="취소" severity="secondary" @click="onClose" />
      <Button label="확인" @click="onConfirm" :disabled="!selectedItems.length" />
    </div>
  </Dialog>
</template>