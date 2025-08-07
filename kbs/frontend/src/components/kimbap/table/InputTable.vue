<script setup>
import { ref, watch, computed, nextTick } from 'vue';
import { format } from 'date-fns';
import SingleSelectModal from '@/components/kimbap/modal/singleselect.vue'; // 경로는 너의 프로젝트에 맞게 수정해줘!

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
            load: { show: false, label: '불러오기', severity: 'info' },
            refund: { show: false, label: '반품요청', severity: 'help' },
            refundReq: { show: false, label: '반품처리', severity: 'info' }
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
    deleteKey: {
        type: String,
        default: 'ordDCd'
    },
    autoCalculation: {
        type: Object,
        default: () => ({
            enabled: false, // 자동계산 켜기/끄기
            quantityField: 'number', // 수량 필드명
            priceField: 'price', // 단가 필드명
            totalField: 'totalPrice' // 총액 필드명
        })
    },
    selectionMode: {
        type: String,
        default: 'multiple', // 기본은 multiple
        validator: (value) => ['single', 'multiple', null].includes(value)
    },
    showRowCount: {
        type: Boolean,
        default: false
    },
    dateFields: {
        type: Array,
        default: () => []
    },
    enableRowClick: {
        type: Boolean,
        default: false
    },
});

const emit = defineEmits(['update:data', 'dataChange', 'openQtyModal', 'delete', 'reset', 'save', 'load', 'refund', 'handleProductDeleteList', 'rowClick', 'selectionChange', 'locationSelect', 'update:selection']);

// console.log('[InputTable.vue] 실제 columns:', props.columns)

// 행 클릭
// 클릭된 행의 데이터를 부모 컴포넌트로 전달
const handleRowClick = (event) => {
    if (!props.enableRowClick) return;
    console.log('[InputTable.vue] 클릭된 행:', event.data);
    emit('rowClick', event.data);
};

// 날짜 포맷 함수
const formatDate = (date) => {
    if (!date) return '';
    const parsed = new Date(date);
    return isNaN(parsed) ? '' : format(parsed, 'yyyy-MM-dd');
};

// 내부 데이터 관리
const internalData = ref([...props.data]);
const selectedRows = ref([]); // 선택된 행들

// 모달 관련 상태들
const modalVisible = ref(false);
const currentRowData = ref(null); // 현재 수정중인 행 데이터
const currentField = ref(''); // 현재 수정중인 필드명

// 🔥 선택 상태 변경 감지해서 부모에게 전달!
watch(
    selectedRows,
    (newSelection, oldSelection) => {
        console.log('🐛 InputTable 선택 변경!');
        emit('selectionChange', newSelection); // 기존 커스텀 이벤트
        emit('update:selection', newSelection); // ✅ v-model:selection 동작하게 만드는 핵심 코드!
    },
    { deep: true }
);

// 초기화 관련 추가(민준)
watch(
    () => props.data,
    (newVal) => {
        internalData.value = newVal;
    },
    { immediate: true, deep: true }
);

// 모달 데이터는 이제 props로 받아옴!

// 빈 행 생성
const createEmptyRow = () => {
    const emptyRow = {
        id: Date.now(),
        [props.dataKey]: `temp_${Date.now()}` // dataKey에 대한 임시 값 생성
    };
    props.columns.forEach((column) => {
        if (column.type === 'input' || column.type === 'inputsearch') {
            emptyRow[column.field] = column.inputType === 'number' ? 0 : '';
        } else if (column.type === 'calendar') {
            emptyRow[column.field] = null;
        } else {
            emptyRow[column.field] = '';
        }
    });
    return emptyRow;
};

// 행 추가
const addRow = async () => {
    const newRow = createEmptyRow();
    internalData.value.push(newRow);
    emitDataChange();

    // DOM 업데이트 완료 후 스크롤을 맨 아래로 이동
    await nextTick(() => {
        const tableContainer = document.querySelector('.p-datatable-scrollable-body');
        if (tableContainer) {
            tableContainer.scrollTop = tableContainer.scrollHeight;
        }
    });
};

// 행 삭제 (선택된 행들)
const deleteSelectedRows = () => {
    if (selectedRows.value.length === 0) {
        alert('삭제할 행을 선택하세요!');
        return;
    }

    const deleteKey = props.deleteKey;
    const toDeleteServer = selectedRows.value.filter((row) => row[deleteKey]);

    // 서버에 알려야 할 제품 목록 → emit
    if (toDeleteServer.length > 0) {
        emit(
            'handleProductDeleteList',
            toDeleteServer.map((row) => row[deleteKey])
        );
    }

    // 로컬에서 제거
    internalData.value = internalData.value.filter((row) => !selectedRows.value.includes(row));

    selectedRows.value = [];
    emitDataChange();
};

// 데이터 변경 이벤트
const emitDataChange = () => {
    emit('update:data', internalData.value);
    emit('dataChange', internalData.value);
};

// 필드 업데이트
const updateField = (rowData, field, value) => {
    // 음수 방지: 숫자 필드이며 0보다 작으면 무조건 0으로 교정
    const columnDef = props.columns.find((col) => col.field === field);
    if (columnDef?.inputType === 'number') {
        const numValue = Number(value);
        if (!isNaN(numValue) && numValue < 0) {
            value = 0;
        }
    }

    rowData[field] = value;

    // 총액 자동계산
    if (props.autoCalculation.enabled) {
        const { quantityField, priceField, totalField } = props.autoCalculation;

        if (field === quantityField || field === priceField) {
            const quantity = rowData[quantityField] || 0;
            const price = rowData[priceField] || 0;

            if (quantity && price) {
                rowData[totalField] = quantity * price;
            }
        }
    }

    emitDataChange();
};

// 모달 열기 함수 - props에서 데이터 확인!
const openModal = (rowData, field) => {
    console.log('모달 열기!', field); // 디버깅용

    if (props.modalDataSets[field]) {
        currentRowData.value = rowData;
        currentField.value = field;
        modalVisible.value = true;
    } else {
        alert(`${field} 필드의 모달 데이터가 없어 ㅠㅠ`);
    }
};

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
    modalVisible.value = visible;
    if (!visible) {
        resetModalState();
    }
};

const resetModalState = () => {
    modalVisible.value = false;
    currentRowData.value = null;
    currentField.value = '';
    console.log('모달 상태 초기화 완료! 🧹');
};

const getAlignClass = (align) => {
    if (align === 'center') return 'text-center';
    if (align === 'right') return 'text-right';
    return 'text-left';
};

const rowCount = computed(() => internalData.value.length);

defineExpose({
    selectedRows
});
// 엑셀 다운로드 
const downloadExcel = () => {
  import('xlsx').then((xlsx) => {
    let rowsToDownload;

    if (selectedRows.value.length > 0) {
      rowsToDownload = selectedRows.value; // ✅ selected.value → selectedRows.value로 수정
    } else {
      rowsToDownload = props.data;
    }

    // ⛔ exportColumns 제거하고 → ✅ props.columns 사용
    const headerMap = {};
    props.columns.forEach(col => {
      if (col.field) {
        headerMap[col.field] = col.header || col.field;
      }
    });

    const converted = rowsToDownload.map(row => {
      const newRow = {};
      for (const key in headerMap) {
        newRow[headerMap[key]] = row[key] ?? '';
      }
      return newRow;
    });

    const worksheet = xlsx.utils.json_to_sheet(converted);
    const workbook = xlsx.utils.book_new();
    xlsx.utils.book_append_sheet(workbook, worksheet, 'Sheet1');

    const filename = rowsToDownload.length === 1
      ? `${rowsToDownload[0][props.dataKey] || 'item'}.xlsx`
      : `${props.title || 'data'}.xlsx`;

    xlsx.writeFile(workbook, filename);
  });
};
</script>

<template>
    <div>
        <div class="border p-6 border-gray-200 rounded-lg bg-white" :style="{ height: props.height }">
            <div class="flex justify-between items-center mb-4">
                <div>
                    <h2 class="text-lg mb-0 font-semibold">{{ title }}</h2>
                    <h3 v-if="showRowCount" class="text-base text-gray-600 mb-0 mt-0">검색결과 {{ rowCount }}건</h3>
                </div>

                <div v-if="buttonPosition === 'top' || buttonPosition === 'both'" class="flex justify-end gap-2">
                    <!-- 슬롯 버튼들 -->
                    <slot name="top-buttons"></slot>
                    <!-- 기본 버튼들 -->
                    <Button v-if="buttons.delete?.show" :label="buttons.delete.label" :severity="buttons.delete.severity" @click="$emit('delete')" />
                    <Button v-if="buttons.reset?.show" :label="buttons.reset.label" :severity="buttons.reset.severity" @click="$emit('reset')" />
                    <Button v-if="buttons.save?.show" :label="buttons.save.label" :severity="buttons.save.severity" @click="$emit('save')" />
                    <Button v-if="buttons.load?.show" :label="buttons.load.label" :severity="buttons.load.severity" @click="$emit('load')" />
                    <Button v-if="buttons.refund?.show" :label="buttons.refund.label" :severity="buttons.refund.severity" @click="$emit('refund')" />
                    <Button v-if="buttons.refundReq?.show" :label="buttons.refundReq.label" :severity="buttons.refundReq.severity" @click="$emit('refundReq')" />
                    <Button v-if="buttons.location?.show" :label="buttons.location.label" :severity="buttons.location.severity" @click="$emit('location')" />
                    <Button v-if="buttons.excel?.show" icon="pi pi-file-excel" :label="buttons.excel.label || '엑셀 다운로드'" :severity="buttons.excel.severity || 'success'" class="p-button-sm" @click="downloadExcel" />

                    <!-- 행 관리 버튼들 -->
                    <template v-if="enableRowActions">
                        <Button v-if="enableSelection && selectedRows.length > 0" :label="`${selectedRows.length}개 삭제`" icon="pi pi-trash" severity="danger" @click="deleteSelectedRows" />
                        <Button label="행 추가" icon="pi pi-plus" outlined severity="info" @click="addRow" />
                    </template>
                </div>
            </div>

            <DataTable
                :value="internalData"
                :tableStyle="{ minWidth: '50rem', tableLayout: 'fixed' }"
                showGridlines
                responsiveLayout="scroll"
                v-model:selection="selectedRows"
                :dataKey="props.dataKey"
                size="large"
                :selectionMode="enableSelection ? selectionMode : null"
                scrollable
                :scrollHeight="scrollHeight"
                :style="{ border: '1px solid #e5e7eb' }"
                @rowClick="props.enableRowClick ? handleRowClick : undefined"
                stripedRows
            >
                <!-- 선택 체크박스 컬럼 -->
                <Column v-if="enableSelection" :selectionMode="selectionMode" headerStyle="width: 3rem"> </Column>

                <!-- 데이터 컬럼들 -->
                <Column v-for="column in columns" :key="column.field" :header="column.header" :headerClass="getAlignClass(column.align)" :bodyClass="getAlignClass(column.align)" :style="column.width ? { width: column.width } : {}">
                    <template #body="slotProps">
                        <!-- <template v-if="column.type === 'readonly'">
                            <span>
                                {{
                                props.dateFields.includes(column.field)
                                    ? formatDate(slotProps.data[column.field])
                                    : (column.formatter ? column.formatter(slotProps.data[column.field]) : slotProps.data[column.field])
                                }}
                            </span>
                        </template> -->

                        <template v-if="column.type === 'readonly'">
                            <span>
                                {{
                                    props.dateFields.includes(column.field)
                                        ? formatDate(slotProps.data[column.field])
                                        : column.formatter
                                          ? column.formatter.length === 1
                                              ? column.formatter(slotProps.data[column.field]) // 단일 값만 넘김
                                              : column.formatter(slotProps.data) // row 전체를 넘김
                                          : slotProps.data[column.field]
                                }}
                            </span>
                        </template>

                        <template v-else-if="column.type === 'input'">
                            <div class="flex items-center border rounded h-10 w-full overflow-hidden">
                                <input
                                    :value="slotProps.data[column.field]"
                                    @input="updateField(slotProps.data, column.field, $event.target.value)"
                                    :type="column.inputType || 'text'"
                                    :readonly="column.readonly"
                                    :disabled="column.disabled"
                                    :placeholder="column.placeholder"
                                    :min="column.inputType === 'number' ? 0 : undefined"
                                    :class="['border-none outline-none flex-1 bg-transparent px-3 py-2 min-w-0', getAlignClass(column.align)]"
                                />
                            </div>
                        </template>

                        <!-- inputsearch 타입에서 모달 연결 -->
                        <template v-else-if="column.type === 'inputsearch'">
                            <div class="flex items-center border rounded h-10 w-full overflow-hidden">
                                <input
                                    :value="slotProps.data[column.field]"
                                    @input="updateField(slotProps.data, column.field, $event.target.value)"
                                    :type="column.inputType || 'text'"
                                    :readonly="column.readonly"
                                    :disabled="column.disabled"
                                    :placeholder="column.placeholder"
                                    :class="['border-none outline-none flex-1 bg-transparent px-3 py-2 min-w-0', getAlignClass(column.align)]"
                                />
                                <div v-if="column.suffixIcon" class="flex items-center justify-center px-2 cursor-pointer text-gray-400 hover:text-blue-500 flex-shrink-0" @click.stop="openModal(slotProps.data, column.field)">
                                    <i :class="[column.suffixIcon, 'text-xs']" />
                                </div>
                            </div>
                        </template>
                        <template v-else-if="column.type === 'select'">
                            <div class="flex items-center border rounded w-full h-10">
                                <select v-model="slotProps.data[column.field]" class="flex-1 bg-transparent px-3 py-2 outline-none">
                                    <option v-for="opt in column.options" :key="opt[column.optionValue]" :value="opt[column.optionValue]">
                                        {{ opt[column.optionLabel] }}
                                    </option>
                                </select>
                            </div>
                        </template>
                        <template v-else-if="column.type === 'calendar'">
                            <Calendar :modelValue="slotProps.data[column.field]" @update:modelValue="updateField(slotProps.data, column.field, $event)" dateFormat="yy-mm-dd" showIcon class="w-full" />
                        </template>

                        <template v-else-if="column.type === 'clickable'">
                            <span class="text-blue-600 underline cursor-pointer" @click.stop="emit('rowClick', slotProps.data)">
                                {{ props.dateFields.includes(column.field) ? formatDate(slotProps.data[column.field]) : slotProps.data[column.field] }}
                            </span>
                        </template>
                        <template v-else-if="column.type === 'button'">
                            <Button :label="column.buttonLabel || '버튼'" :severity="column.buttonSeverity || 'info'" size="small" @click.stop="$emit(column.buttonEvent || 'buttonClick', slotProps.data, column)" />
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
