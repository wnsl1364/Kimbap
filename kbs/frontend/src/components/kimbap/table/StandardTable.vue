<script setup>
import { ref } from 'vue';
import Button from 'primevue/button';

const props = defineProps({
    data: { type: Array, default: () => [] },
    height: { type: String, default: '500px' },
    columns: { type: Array, default: () => [] },
    title: { type: String, default: '' },
    dataKey: { type: String, default: 'id' },
    scrollHeight: { type: String, default: '400px' }, // 예: '300px', 'flex', '100%'
    tableMinWidth: { type: String, default: '50rem' } // 👈 추가
});

const emit = defineEmits(['view-history']);
const selected = ref();

const handleClick = (rowData) => {
    emit('view-history', rowData);
};
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
        >
            <Column selectionMode="single" headerStyle="width: 3rem" />
            <Column
                v-for="col in columns"
                :key="col.field"
                :field="col.field"
                :header="col.header"
            />
            <!-- ✅ slot 방식으로 이력조회 버튼 컬럼 렌더링 -->
            <Column header="이력조회">
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
