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
  showRowCount: { type: Boolean, default: false }
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
</script>

<template>
  <div class="border p-6 border-gray-200 rounded-lg bg-white" :style="{ height: props.height }">
    <div>
      <h2 v-if="title" class="text-lg font-semibold mb-4">{{ title }}</h2>
      <h3 v-if="showRowCount" class="text-base text-gray-600 mb-0 mt-0">검색결과 {{ rowCount }}건</h3>
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
        <Column
          v-if="!col.slot"
          :field="col.field"
          :header="col.header"
          :headerClass="getAlignClass(col)"
          :bodyClass="getAlignClass(col)"
        />
        <Column
          v-else
          :header="col.header"
          :headerClass="getAlignClass(col)"
          :bodyClass="getAlignClass(col)"
        >
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
