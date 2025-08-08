<script setup>
import { ref, watch, computed } from 'vue';
import Button from 'primevue/button';

const props = defineProps({
    data: { type: Array, default: () => [] },
    height: { type: String, default: '500px' },
    columns: { type: Array, default: () => [] },
    title: { type: String, default: '' },
    dataKey: { type: String, default: 'id' },
    scrollHeight: { type: String, default: '400px' },
    tableMinWidth: { type: String, default: '50rem' },
    showHistoryButton: { type: Boolean, default: true },
    selectable: { type: Boolean, default: true },
    hoverable: { type: Boolean, default: false },
    showRowCount: { type: Boolean, default: false },
    showExcelDownload: { type: Boolean, default: false },
    exportColumns: { type: Array, default: () => [] },
    exportData: { type: Array, default: null }
});

const emit = defineEmits(['view-history', 'row-select', 'clear-selection']);
const selected = ref([]);

const handleClick = (rowData) => {
    emit('view-history', rowData);
};

watch(selected, (val) => {
    if (val.length === 0) {
        emit('clear-selection');
    }
});

const rowCount = computed(() => props.data.length);

// 정렬 클래스
const getAlignClass = (col) => {
    if (col.align === 'right') return 'text-right';
    if (col.align === 'center') return 'text-center';
    return 'text-left';
};

const downloadExcel = () => {
  import('xlsx').then((xlsx) => {
    let rowsToDownload;

    if (props.exportData?.length && selected.value.length > 0) {
      // ✅ exportData + 선택된 항목 → 매칭된 것만 추출
      const selectedKeys = selected.value.map(row => row[props.dataKey]);
      rowsToDownload = props.exportData.filter(row =>
        selectedKeys.includes(row[props.dataKey])
      );
    } else if (selected.value.length > 0) {
      // ✅ exportData가 없으면 selected 값만 다운
      rowsToDownload = selected.value;
    } else {
      // ✅ 아무 선택 없을 경우 전체 exportData or data 다운
      rowsToDownload = props.exportData ?? props.data;
    }

    const headerMap = {};
    props.exportColumns.forEach(col => {
      if (col.field) headerMap[col.field] = col.header;
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
    <div class="border p-4 border-gray-200 rounded-lg bg-white" :style="{ height: props.height }">
        <div class="flex justify-between items-start mb-0">
            <!-- 왼쪽: 제목 + 검색결과 -->
            <div>
                <h2 v-if="title" class="text-lg font-semibold mb-1">{{ title }}</h2>
                <h3 v-if="showRowCount" class="text-base text-gray-600 mt-0 mb-1">검색결과 {{ rowCount }}건</h3>
            </div>

            <!-- 오른쪽: 엑셀 다운로드 버튼 -->
            <Button v-if="props.showExcelDownload" icon="pi pi-file-excel" label="엑셀 다운로드" class="p-button-sm" severity="success" @click="downloadExcel" />
        </div>

        <DataTable
            :value="data"
            :tableStyle="{ minWidth: props.tableMinWidth }"
            showGridlines
            responsiveLayout="scroll"
            v-model:selection="selected"
            :dataKey="dataKey"
            size="large"
            scrollable
            :scrollHeight="scrollHeight"
            @rowSelect="$emit('row-select', $event.data)"
            @rowClick="$emit('row-click', $event.data)"
            :class="{ 'hoverable-rows': props.hoverable }"
        >
            <Column v-if="props.selectable" selectionMode="multiple" headerStyle="width: 3rem" />

            <!-- 일반/슬롯 컬럼 렌더링 -->
            <template v-for="col in columns" :key="col.field">
                <Column v-if="!col.slot" :field="col.field" :header="col.header" headerClass="text-center" :bodyClass="getAlignClass(col)" />
                <Column v-else :header="col.header" headerClass="text-center" :bodyClass="getAlignClass(col)">
                    <template #body="slotProps">
                        <!-- 기본 text 정렬용 slot -->
                        <div :class="getAlignClass(col)">
                            {{ slotProps.data[col.field] }}
                        </div>
                    </template>
                </Column>
            </template>

            <!-- 이력조회 버튼 -->
            <Column header="이력조회" v-if="props.showHistoryButton">
                <template #body="slotProps">
                    <Button label="이력조회" size="small" text severity="info" @click="handleClick(slotProps.data)" />
                </template>
            </Column>
        </DataTable>
    </div>
</template>

<style scoped>
:deep(.hoverable-rows .p-datatable-tbody > tr:hover) {
    background-color: #f0f9ff !important;
    cursor: pointer;
    transition: background-color 0.2s ease;
}
</style>
