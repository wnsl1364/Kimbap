<script setup>
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
})

const emit = defineEmits(['update:data'])

const updateField = (field, value) => {
    emit('update:data', { ...props.data, [field]: value })
}
</script>
<template>
  <div class="bg-white rounded shadow p-6">
    <h2 v-if="title" class="text-lg font-semibold mb-4">{{ title }}</h2>
    <DataTable :value="data" :tableStyle="{ minWidth: '50rem' }" showGridlines :rows="10" responsiveLayout="scroll" v-model:selection="selected" dataKey="id" size="large">
        <Column v-for="column in columns" :key="column.field" :header="column.header">
            <template #body="slotProps">
                <template v-if="column.type === 'readonly'">
                    <span>{{ slotProps.data[column.field] }}</span>
                </template>
                <template v-else-if="column.type === 'input'">
                    <input
                        v-model="slotProps.data[column.field]"
                        :type="column.inputType || 'text'"
                        class="border rounded w-full p-1 h-9"
                    />
                </template>
                <template v-else-if="column.type === 'calendar'">
                    <Calendar
                        v-model="slotProps.data[column.field]"
                        dateFormat="yy-mm-dd"
                        showIcon
                        class="w-full"
                    />
                </template>
            </template>
        </Column>
    </DataTable>
  </div>
</template>
