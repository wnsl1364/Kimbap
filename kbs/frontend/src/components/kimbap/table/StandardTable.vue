<script setup>
import { ref } from 'vue';
import Button from 'primevue/button';

const props = defineProps({
    data: { type: Array, default: () => [] },
    columns: { type: Array, default: () => [] },
    title: { type: String, default: '' }
});

const emit = defineEmits(['view-history']);
const selected = ref();

const handleClick = (rowData) => {
    emit('view-history', rowData);
};
</script>

<template>
    <div class="card">
        <h2 v-if="title" class="text-lg font-semibold mb-4">{{ title }}</h2>
        <DataTable
            :value="data"
            :tableStyle="{ minWidth: '50rem' }"
            showGridlines
            :rows="10"
            responsiveLayout="scroll"
            v-model:selection="selected"
            dataKey="id"
            size="large"
        >
            <Column selectionMode="multiple" headerStyle="width: 3rem" />
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
