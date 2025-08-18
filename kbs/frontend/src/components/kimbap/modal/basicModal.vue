<script setup>
import { ref, watch } from 'vue';
import Dialog from 'primevue/dialog';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Button from 'primevue/button';

// props 정의
const props = defineProps({
    visible: Boolean,
    items: Array,
    columns: Array,
    itemKey: { type: String, default: 'id' },
    fetchItems: Function,
    selectedItem: Object,
    titleName: { type: String, default: '' },
    titleCode: { type: String, default: '' }, 
});

// emits 정의
const emit = defineEmits(['update:visible']);

// 검색용 상태
const filteredItems = ref([]);

// 🎯 기본 스타일 그대로 유지하는 PassThrough
const dialogPT = {
    root: { class: '' },
    mask: { class: '' },
    content: { class: '' }
};

const dataTablePT = {
    root: { class: '' },
    wrapper: { class: '' },
    table: { class: '' }
};

const buttonPT = {
    root: { class: '' }
};

watch(
    () => props.items,
    (newItems) => {
        console.log('🔄 BasicModal items 변경:', newItems);
        filteredItems.value = newItems || [];
        console.log('🔄 BasicModal filteredItems 업데이트:', filteredItems.value);
    },
    { immediate: true }
);

function onClose() {
    emit('update:visible', false);
}
</script>

<template>
    <Dialog 
        :visible="visible" 
        @update:visible="emit('update:visible', $event)" 
        modal 
        :style="{ width: '60rem' }" 
        :closable="false"
        :pt="dialogPT"
    >
        <!-- ✅ 헤더 커스텀 -->
        <template #header>
            <div class="flex justify-between items-center">
                <h2 class="text-lg font-semibold">
                📜 {{ props.titleName || '-' }} ({{ props.titleCode || '-' }}) 이력 조회
                </h2>
            </div>
        </template>
        
        <!-- 🔧 핵심 수정 2: PassThrough 추가 -->
        <DataTable 
            :value="filteredItems" 
            :dataKey="props.itemKey" 
            tableStyle="min-width: 50rem" 
            showGridlines 
            scrollable 
            scrollHeight="384px"
            :pt="dataTablePT"
        >
            <Column v-for="col in columns" :key="String(col.field)" :field="String(col.field)" :header="col.header">
                <template #body="slotProps">
                    {{ slotProps.data?.[String(col.field)] ?? '-' }}
                </template>
            </Column>
        </DataTable>

        <!-- 닫기 버튼 -->
        <div class="flex justify-end gap-2 mt-4">
            <Button 
                label="닫기" 
                severity="secondary" 
                @click="onClose" 
                :pt="buttonPT"
            />
        </div>
    </Dialog>
</template>