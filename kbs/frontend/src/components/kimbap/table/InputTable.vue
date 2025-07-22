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
  <div>
    <DataTable :value="data" :tableStyle="{ minWidth: '50rem' }" showGridlines :rows="10" responsiveLayout="scroll" v-model:selection="selected" dataKey="id" size="large">
        <Column v-for="column in columns" :key="column.field" :header="column.header">
            <template #body="slotProps">
                <template v-if="column.type === 'readonly'">
                    <span>{{ slotProps.data[column.field] }}</span>
                </template>
                <template v-else-if="column.type === 'input'">
                    <div class="flex items-center border rounded w-full h-10">
                        <input
                            v-model="slotProps.data[column.field]"
                            :type="column.inputType || 'text'"
                            class="border-none outline-none flex-1 bg-transparent px-3 py-2"
                        />
                        <i
                            v-if="column.suffixIcon"
                            :class="[column.suffixIcon, 'cursor-pointer text-gray-400 px-3 py-2']"
                            @click="$emit(column.suffixEvent, slotProps.data)"
                        />
                    </div>
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
