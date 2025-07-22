<script setup>
const props = defineProps({
    data: Array,
    columns: Array
})
</script>
<template>
    <DataTable :value="data" :tableStyle="{ minWidth: '50rem' }" showGridlines :rows="10" responsiveLayout="scroll" v-model:selection="selected" dataKey="id" size="large">
        <Column v-for="col in columns" :key="col.field" :header="col.header">
            <template #body="slotProps">
                <template v-if="col.type === 'input'">
                    <input 
                        v-model="slotProps.data[col.field]"
                        :type="col.inputType || 'text'"
                        class="border border-gray-300 rounded p-1 w-full h-10"
                    />
                </template>
                <template v-else>
                    {{ slotProps.data[col.field] }}
                </template>
            </template>
        </Column>
    </DataTable>
</template>
