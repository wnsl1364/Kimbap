<script setup>
import { ref , watch } from 'vue';
import Button from 'primevue/button';

const props = defineProps({
    data: { type: Array, default: () => [] },
    height: { type: String, default: '500px' },
    columns: { type: Array, default: () => [] },
    title: { type: String, default: '' },
    dataKey: { type: String, default: 'id' },
    scrollHeight: { type: String, default: '400px' }, // 예: '300px', 'flex', '100%'
    tableMinWidth: { type: String, default: '50rem' }, // 👈 추가
    showHistoryButton: { type: Boolean, default: true }, // 이력조회 숨기기 추가
    selectable: { type: Boolean, default: true }, // select 숨기기 추가
    hoverable: { type: Boolean, default: false } // 행 hover 기능 추가
});

const emit = defineEmits(['view-history', 'row-select', 'clear-selection']);
const selected = ref([]);

const handleClick = (rowData) => {
    emit('view-history', rowData);
};

// 선택 해제 감지해서 이벤트 emit
watch(selected, (val) => {
  if (val.length === 0) {
    emit('clear-selection'); // 부모에게 선택 해제 알림
  }
});
</script>

<template>
    <div class="border p-6 border-gray-200 rounded-lg bg-white" :style="{ height: props.height }">
        <h2 v-if="title" class="text-lg font-semibold mb-4">{{ title }}</h2>
        <DataTable
            :value="data"
            :tableStyle="{ minWidth: '50rem' }"
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
            <Column
                v-for="col in columns"
                :key="col.field"
                :field="col.field"
                :header="col.header"
            />
            <!-- ✅ slot 방식으로 이력조회 버튼 컬럼 렌더링 -->
            <Column header="이력조회" v-if="props.showHistoryButton">
                <template #body="slotProps">
                    <Button
                        label="이력조회"
                        size="small"
                        text
                        severity="info"
                        @click="handleClick(slotProps.data)"
                    />
                </template>
            </Column>
        </DataTable>
    </div>
</template>
<style scoped>
/* PrimeVue DataTable row hover 효과 */
:deep(.hoverable-rows .p-datatable-tbody > tr:hover) {
  background-color: #f0f9ff !important; /* 연한 하늘색 */
  cursor: pointer;
  transition: background-color 0.2s ease;
}
</style>