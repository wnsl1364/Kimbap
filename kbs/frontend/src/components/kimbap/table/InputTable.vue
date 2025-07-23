<script setup>
import { ref } from 'vue'

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
    }   
})

const emit = defineEmits(['update:data', 'dataChange'])

// 내부 데이터 관리
const internalData = ref([...props.data])
const selectedRows = ref([]) // 선택된 행들

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
        alert('삭제할 행을 선택하세요.')
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
    
    // 총액 자동계산
    if (field === 'number' || field === 'price') {
        if (rowData.number && rowData.price) {
            rowData.totalPrice = rowData.number * rowData.price
        }
    }
    
    emitDataChange()
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
                <Button v-if="buttons.delete?.show" :label="buttons.delete.label" :severity="buttons.delete.severity" />
                <Button v-if="buttons.reset?.show" :label="buttons.reset.label" :severity="buttons.reset.severity" />
                <Button v-if="buttons.save?.show" :label="buttons.save.label" :severity="buttons.save.severity" />
                <Button v-if="buttons.load?.show" :label="buttons.load.label" :severity="buttons.load.severity" />
                
                <!-- 행 관리 버튼들 -->
                <template v-if="enableRowActions">
                    <Button v-if="enableSelection && selectedRows.length > 0"
                        :label="`${selectedRows.length}개 삭제`" 
                        icon="pi pi-trash" 
                        severity="danger" 
                        @click="deleteSelectedRows" />
                    <Button label="행 추가" 
                        icon="pi pi-plus" 
                        severity="help" 
                        @click="addRow" />
                </template>
                
                
            </div>
        </div>
        
        <DataTable 
            :value="internalData" 
            :tableStyle="{ minWidth: '50rem' }" 
            showGridlines
            responsiveLayout="scroll"
            v-model:selection="selectedRows"
            dataKey="id" 
            size="large"
            :selectionMode="enableSelection ? 'multiple' : null"
            scrollable
            :scrollHeight="scrollHeight"
            >
            
            <!-- 선택 체크박스 컬럼 -->
            <Column v-if="enableSelection" 
                selectionMode="multiple" 
                headerStyle="width: 1rem">
            </Column>
            
            <!-- 데이터 컬럼들 -->
            <Column v-for="column in columns" :key="column.field" :header="column.header"
                :headerClass="getAlignClass(column.align)" :bodyClass="getAlignClass(column.align)"
                :style="{ width: column.width || 'auto' }">
                <template #body="slotProps">
                    <template v-if="column.type === 'readonly'">
                        <span>{{ slotProps.data[column.field] }}</span>
                    </template>
                    
                    <template v-else-if="column.type === 'input'">
                        <div class="flex items-center border rounded w-full h-10">
                            <input 
                                :value="slotProps.data[column.field]"
                                @input="updateField(slotProps.data, column.field, $event.target.value)"
                                :type="column.inputType || 'text'"
                                :readonly="column.readonly" 
                                :disabled="column.disabled"
                                :placeholder="column.placeholder"
                                :class="['border-none outline-none flex-1 bg-transparent px-3 py-2', getAlignClass(column.align)]" />
                        </div>
                    </template>
                    
                    <template v-else-if="column.type === 'calendar'">
                        <Calendar 
                            :modelValue="slotProps.data[column.field]"
                            @update:modelValue="updateField(slotProps.data, column.field, $event)"
                            dateFormat="yy-mm-dd"
                            showIcon 
                            class="w-full" />
                    </template>
                </template>
            </Column>
        </DataTable>
        </div>
    </div>
</template>