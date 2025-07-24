<script setup>
import { onMounted } from 'vue';
import Dialog from 'primevue/dialog';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Button from 'primevue/button';

const props = defineProps({
    visible: Boolean,
    items: Array,
    itemKey: { type: String, default: 'id' },
    columns: Array,
    fetchItems: Function
});

const emit = defineEmits(['update:visible']);

onMounted(async () => {
    if (props.fetchItems) {
        const res = await props.fetchItems();
        emit('update:items', res);
    }
});

function onClose() {
    emit('update:visible', false);
}
</script>

<template>
    <Dialog :visible="visible" @update:visible="emit('update:visible', $event)" modal header="이력 목록" :style="{ width: '60rem' }" :closable="false">
        <!-- 이력 테이블 -->
        <DataTable :value="props.items" :dataKey="itemKey" tableStyle="min-width: 50rem" showGridlines>
            <Column v-for="col in columns" :key="col.field" :field="col.field" :header="col.header" />
        </DataTable>

        <div class="flex justify-end gap-2 mt-4">
            <Button label="닫기" severity="secondary" @click="onClose" />
        </div>
    </Dialog>
</template>
