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
    fetchItems: Function
});

// emits 정의
const emit = defineEmits(['update:visible']);

// 검색용 상태
const filteredItems = ref([]);

// 모달 열릴 때 fetchItems() 실행
watch(
  () => props.items,
  (newItems) => {
    filteredItems.value = JSON.parse(JSON.stringify(newItems)) || []
  },
  { immediate: true }
)

watch(
    () => props.items,
    (newItems) => {
        filteredItems.value = newItems || [];
    },
    { immediate: true }
);

function onClose() {
    emit('update:visible', false);
}

</script>

<template>
    <Dialog :visible="visible" @update:visible="emit('update:visible', $event)" modal header="이력 목록" :style="{ width: '60rem' }" :closable="false">
        <!-- 테이블 -->
        <DataTable :value="filteredItems" dataKey="version" tableStyle="min-width: 50rem" showGridlines>
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
