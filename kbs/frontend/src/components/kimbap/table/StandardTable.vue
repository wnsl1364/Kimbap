<script setup>
import { ref, watch, computed, onMounted, nextTick } from 'vue';
import Button from 'primevue/button';

const props = defineProps({
    data: { type: Array, default: () => [] },
  // 기본 카드 높이를 430px로 통일
  height: { type: String, default: '430px' },
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

// 동적 scrollHeight 계산: 전체 height - (헤더 영역 실제 높이)
const headerRef = ref(null);
const containerRef = ref(null);
const computedScrollHeight = computed(() => {
  // 명시적 scrollHeight prop 우선
  if (props.scrollHeight && !props.height) return props.scrollHeight;
  if (!containerRef.value) return props.scrollHeight;
  const containerH = containerRef.value.clientHeight;
  const headerH = headerRef.value ? headerRef.value.clientHeight : 0;
  // 패딩  (p-4 => 1rem = 16px 상하 32px) 고려
  const padding = 32;
  const body = containerH - headerH - padding;
  return body > 100 ? body + 'px' : props.scrollHeight; // 최소 안전값
});

// 정렬 클래스
const getAlignClass = (col) => {
    if (col.align === 'right') return 'text-right';
    if (col.align === 'center') return 'text-center';
    return 'text-left';
};

// 🎯 텍스트 컬러 클래스 생성 함수 (새로 추가!)
const getTextColorClass = (col, rowData) => {
    if (col.textColor && typeof col.textColor === 'function') {
        return col.textColor(rowData);
    }
    return '';
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
  <div ref="containerRef" class="border p-4 border-gray-200 rounded-lg bg-white standard-table-flex flex flex-col" :style="props.height ? { height: props.height } : {}">
    <div ref="headerRef" class="flex justify-between items-start mb-2 shrink-0">
            <!-- 왼쪽: 제목 + 검색결과 -->
            <div>
                <h2 v-if="title" class="text-lg font-semibold mb-1">{{ title }}</h2>
                <h3 v-if="showRowCount" class="text-base text-gray-600 mt-0 mb-1">검색결과 {{ rowCount }}건</h3>
            </div>

            <!-- 오른쪽: 엑셀 다운로드 버튼 -->
            <Button v-if="props.showExcelDownload" icon="pi pi-file-excel" label="엑셀 다운로드" class="p-button-sm" severity="success" @click="downloadExcel" />
        </div>

        <div class="flex-1 min-h-0 overflow-hidden">
          <DataTable
              :value="data"
              :tableStyle="{ minWidth: props.tableMinWidth }"
              showGridlines
              responsiveLayout="scroll"
              v-model:selection="selected"
              :dataKey="dataKey"
              size="large"
              scrollable
              :scrollHeight="computedScrollHeight"
              @rowSelect="$emit('row-select', $event.data)"
              @rowClick="$emit('row-click', $event.data)"
              :class="[{ 'hoverable-rows': props.hoverable }, 'h-full']"
          >
            <Column v-if="props.selectable" selectionMode="multiple" headerStyle="width: 3rem" />

            <!-- 일반/슬롯 컬럼 렌더링 (textColor 지원 추가!) -->
            <template v-for="col in columns" :key="col.field">
                <Column v-if="!col.slot" :field="col.field" :header="col.header" headerClass="text-center" :bodyClass="getAlignClass(col)">
                    <!-- 🎯 textColor 함수가 있는 경우 커스텀 렌더링 -->
                    <template v-if="col.textColor" #body="slotProps">
                        <div :class="[getAlignClass(col), getTextColorClass(col, slotProps.data)]">
                            {{ slotProps.data[col.field] }}
                        </div>
                    </template>
                </Column>
                <Column v-else :header="col.header" headerClass="text-center" :bodyClass="getAlignClass(col)">
                    <template #body="slotProps">
                        <!-- 기본 text 정렬용 slot (textColor도 적용) -->
                        <div :class="[getAlignClass(col), getTextColorClass(col, slotProps.data)]">
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
    </div>
</template>

<style scoped>
:deep(.hoverable-rows .p-datatable-tbody > tr:hover) {
    background-color: #f0f9ff !important;
    cursor: pointer;
    transition: background-color 0.2s ease;
}
</style>
<style>
/* flex 높이 채움 시 DataTable wrapper가 남은 공간 올바르게 채우도록 */
.standard-table-flex .p-datatable-wrapper { height: 100%; }
.standard-table-flex .p-datatable { height: 100%; display: flex; flex-direction: column; }
.standard-table-flex .p-datatable-scrollable-wrapper { flex: 1 1 auto; }
</style>