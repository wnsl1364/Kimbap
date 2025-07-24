<script setup>
import { ref, watch } from 'vue'
import SingleSelectModal from '@/components/kimbap/modal/singleselect.vue' // 경로는 너의 프로젝트에 맞게 수정해줘!

const props = defineProps({
    data: {
        type: Array,
        default: () => []
    },
    height: {
        type: String,
        default: '55vh'
    },
    columns: {
        type: Array,
        default: () => []
    },
    title: {
        type: String,
        default: ''
    },
    buttons: {
        type: Object,
        default: () => ({
            save: { show: true, label: '저장', severity: 'success' },
            reset: { show: true, label: '초기화', severity: 'secondary' },
            delete: { show: false, label: '삭제', severity: 'danger' },
            load: { show: false, label: '불러오기', severity: 'info' }
        })
    },
    buttonPosition: {
        type: String,
        default: 'top',
        validator: (value) => ['top', 'bottom', 'both'].includes(value)
    },
    // 행 추가/삭제 기능 on/off
    enableRowActions: {
        type: Boolean,
        default: true
    },
    // 선택 기능 활성화
    enableSelection: {
        type: Boolean,
        default: true
    },
    // 스크롤 높이
    scrollHeight: {
        type: String,
        default: '400px'
    },
    // 모달 데이터 설정
    modalDataSets: {
        type: Object,
        default: () => ({})
    },
    // 데이터 키 설정
    dataKey: {
         type: String,
         required: true
    },
})

const emit = defineEmits([
  'update:data',
  'dataChange',
  'openQtyModal',
  'delete',
  'reset',
  'save',
  'load'
])


// 내부 데이터 관리
const internalData = ref([...props.data])
const selectedRows = ref([]) // 선택된 행들

// 모달 관련 상태들
const modalVisible = ref(false)
const currentRowData = ref(null) // 현재 수정중인 행 데이터
const currentField = ref('') // 현재 수정중인 필드명


// 초기화 관련 추가(민준)
watch(
  () => props.data,
  (newVal) => {
    internalData.value = newVal
  },
  { immediate: true, deep: true }
)

// 모달 데이터는 이제 props로 받아옴!

// 빈 행 생성
const createEmptyRow = () => {
    const emptyRow = { id: Date.now() }
    props.columns.forEach(column => {
        if (column.type === 'input') {
            emptyRow[column.field] = column.inputType === 'number' ? 0 : ''
        } else if (column.type === 'calendar') {
            emptyRow[column.field] = null
        } else {
            emptyRow[column.field] = ''
        }
    })
    return emptyRow
}

// 행 추가
const addRow = () => {
    const newRow = createEmptyRow()
    internalData.value.push(newRow)
    emitDataChange()
}

// 행 삭제 (선택된 행들)
const deleteSelectedRows = () => {
    if (selectedRows.value.length === 0) {
        alert('삭제할 행을 선택하세요! ㅠㅠ')
        return
    }
    const selectedIds = selectedRows.value.map(row => row.id)
    internalData.value = internalData.value.filter(row => !selectedIds.includes(row.id))
    selectedRows.value = [] // 선택 초기화
    emitDataChange()
}

// 데이터 변경 이벤트
const emitDataChange = () => {
    emit('update:data', internalData.value)
    emit('dataChange', internalData.value)
}

// 필드 업데이트
const updateField = (rowData, field, value) => {
    rowData[field] = value

    // 총액 자동계산 - 완전 중요해! 🧮
    if (field === 'number' || field === 'price') {
        if (rowData.number && rowData.price) {
            rowData.totalPrice = rowData.number * rowData.price
        }
    }

    emitDataChange()
}

// 모달 열기 함수 - props에서 데이터 확인!
const openModal = (rowData, field) => {
    console.log('모달 열기!', field) // 디버깅용
    
    if (props.modalDataSets[field]) {
        currentRowData.value = rowData
        currentField.value = field
        modalVisible.value = true
    } else {
        alert(`${field} 필드의 모달 데이터가 없어 ㅠㅠ`)
    }
}

// 모달에서 선택 완료했을 때
const handleModalSelect = (selectedItem) => {
  if (currentRowData.value && currentField.value && selectedItem) {
    const modalConfig = props.modalDataSets[currentField.value];

    if (modalConfig?.mappingFields) {
      for (const [targetField, sourceField] of Object.entries(modalConfig.mappingFields)) {
        updateField(currentRowData.value, targetField, selectedItem[sourceField]);
      }
    } else {
      // fallback 처리 (displayField만 있을 경우)
      const displayValue = selectedItem[modalConfig.displayField];
      updateField(currentRowData.value, currentField.value, displayValue);
    }

    console.log(`${currentField.value} 모달에서 선택됨`, selectedItem);
  }
  resetModalState();
};

const handleModalClose = (visible) => {
    modalVisible.value = visible
    if (!visible) {
        resetModalState()
    }
}

const resetModalState = () => {
    modalVisible.value = false
    currentRowData.value = null
    currentField.value = ''
    console.log('모달 상태 초기화 완료! 🧹')
}

const getAlignClass = (align) => {
    if (align === 'center') return 'text-center'
    if (align === 'right') return 'text-right'
    return 'text-left'
}
</script>

<template>
    <div>
        <div class="border p-6 border-gray-200 rounded-lg bg-white" :style="{ height: props.height }">
            <div class="flex justify-between items-center mb-4">
                <h2 class="text-lg mb-0 font-semibold">{{ title }}</h2>

                <div v-if="buttonPosition === 'top' || buttonPosition === 'both'" class="flex justify-end gap-2">
                    <!-- 슬롯 버튼들 -->
                    <slot name="top-buttons"></slot>
                    <!-- 기본 버튼들 -->
                    <Button v-if="buttons.delete?.show" :label="buttons.delete.label" :severity="buttons.delete.severity" @click="$emit('save')"/>
                    <Button v-if="buttons.reset?.show" :label="buttons.reset.label" :severity="buttons.reset.severity" @click="$emit('reset')" />
                    <Button v-if="buttons.save?.show" :label="buttons.save.label" :severity="buttons.save.severity" @click="$emit('delete')"/>
                    <Button v-if="buttons.load?.show" :label="buttons.load.label" :severity="buttons.load.severity" @click="$emit('load')"/>

                    <!-- 행 관리 버튼들 -->
                    <template v-if="enableRowActions">
                        <Button v-if="enableSelection && selectedRows.length > 0" :label="`${selectedRows.length}개 삭제`"
                            icon="pi pi-trash" severity="danger" @click="deleteSelectedRows" />
                        <Button label="행 추가" icon="pi pi-plus" severity="help" @click="addRow" />
                    </template>
                </div>
            </div>

            <DataTable :value="internalData" :tableStyle="{ minWidth: '50rem' }" showGridlines responsiveLayout="scroll"
                v-model:selection="selectedRows" dataKey="id" size="large"
                :selectionMode="enableSelection ? 'multiple' : null" scrollable :scrollHeight="scrollHeight"
                :style="{ border: '1px solid #e5e7eb' }">

                <!-- 선택 체크박스 컬럼 -->
                <Column v-if="enableSelection" selectionMode="multiple" headerStyle="width: 3rem">
                </Column>

                <!-- 데이터 컬럼들 -->
                <Column v-for="column in columns" :key="column.field" :header="column.header"
                    :headerClass="getAlignClass(column.align)" :bodyClass="getAlignClass(column.align)">
                    <template #body="slotProps">
                        <template v-if="column.type === 'readonly'">
                            <span>{{ slotProps.data[column.field] }}</span>
                        </template>

                        <template v-else-if="column.type === 'input'">
                            <div class="flex items-center border rounded w-full h-10">
                                <input :value="slotProps.data[column.field]"
                                    @input="updateField(slotProps.data, column.field, $event.target.value)"
                                    :type="column.inputType || 'text'" :readonly="column.readonly"
                                    :disabled="column.disabled" :placeholder="column.placeholder"
                                    :style="{ width: column.width || 'auto' }"
                                    :class="['border-none outline-none flex-1 bg-transparent px-3 py-2', getAlignClass(column.align)]" />
                            </div>
                        </template>
                        
                        <!-- 여기가 핵심! inputsearch 타입에서 모달 연결 -->
                        <template v-else-if="column.type === 'inputsearch'">
                            <div class="flex items-center border rounded w-full h-10">
                                <input :value="slotProps.data[column.field]"
                                    @input="updateField(slotProps.data, column.field, $event.target.value)"
                                    :type="column.inputType || 'text'"
                                    :readonly="column.readonly" :disabled="column.disabled"
                                    :placeholder="column.placeholder"
                                    :class="['border-none outline-none flex-1 bg-transparent px-3 py-2', getAlignClass(column.align)]" />
                                <i v-if="column.suffixIcon"
                                    :class="[column.suffixIcon, 'cursor-pointer text-gray-400 px-3 py-2 hover:text-blue-500']"
                                    @click.stop="openModal(slotProps.data, column.field)" />
                            </div>
                        </template>
                        
                        <template v-else-if="column.type === 'calendar'">
                            <Calendar :modelValue="slotProps.data[column.field]"
                                @update:modelValue="updateField(slotProps.data, column.field, $event)"
                                dateFormat="yy-mm-dd" showIcon class="w-full" />
                        </template>
                    </template>
                </Column>
            </DataTable>
        </div>

        <!-- 모달 컴포넌트 - props에서 데이터 가져오기 -->
        <SingleSelectModal
            v-model:visible="modalVisible"
            :key="`${currentField}-${Date.now()}`"
            :modelValue="null"
            @update:modelValue="handleModalSelect"
            @update:visible="handleModalClose"
            :items="props.modalDataSets[currentField]?.items || []"
            :columns="props.modalDataSets[currentField]?.columns || []"
            :itemKey="props.dataKey"
        />
    </div>
</template>