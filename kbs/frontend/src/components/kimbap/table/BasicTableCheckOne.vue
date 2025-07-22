<script setup>
import { ref } from 'vue';

const props = defineProps({
    data: {
        type: Array,
        default: () => []
    },
    columns: {
        type: Array,
        default: () => []
    },
    title: {
        type: String,
        default: ''
    }
});

const selected = ref();
</script>
<template>
    <div class="card">
        <h2 v-if="title" class="text-lg font-semibold mb-4">{{ title }}</h2>
        <DataTable :value="data" :tableStyle="{ minWidth: '50rem' }" showGridlines :rows="10" responsiveLayout="scroll" v-model:selection="selected" dataKey="id" size="large">
            <Column selectionMode="single" headerStyle="width: 3rem"></Column>
            <Column 
                v-for="col in columns" 
                :key="col.field" 
                :field="col.field" 
                :header="col.header" />
        </DataTable>
    </div>
</template>
