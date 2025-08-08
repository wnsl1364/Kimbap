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
    // ✅ 추가: 모달 제목 구성용
    titleName: { type: String, default: '' },     // 이름 (예: 공장명 / 거래처명)
    titleCode: { type: String, default: '' }, 
});

// emits 정의
const emit = defineEmits(['update:visible']);

// 검색용 상태
const filteredItems = ref([]);

// 🔧 핵심 수정 1: 중복된 watch 제거하고 하나로 통합
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
    <Dialog :visible="visible" @update:visible="emit('update:visible', $event)" modal :style="{ width: '60rem' }" :closable="false">
        <!-- ✅ 헤더 커스텀 -->
        <template #header>
            <div class="flex justify-between items-center">
                <h2 class="text-lg font-semibold">
                📜 {{ props.titleName || '-' }} ({{ props.titleCode || '-' }}) 이력 조회
                </h2>
            </div>
        </template>
        
        <!-- 🔧 핵심 수정 2: dataKey를 props.itemKey로 동적 설정 -->
        <DataTable 
            :value="filteredItems" 
            :dataKey="props.itemKey" 
            tableStyle="min-width: 50rem" 
            showGridlines 
            scrollable 
            scrollHeight="384px"
        >
            <Column v-for="col in columns" :key="String(col.field)" :field="String(col.field)" :header="col.header">
                <template #body="slotProps">
                    {{ slotProps.data?.[String(col.field)] ?? '-' }}
                </template>
            </Column>
        </DataTable>

        <!-- 닫기 버튼 -->
        <div class="flex justify-end gap-2 mt-4">
            <Button label="닫기" severity="secondary" @click="onClose" />
        </div>
    </Dialog>
</template>