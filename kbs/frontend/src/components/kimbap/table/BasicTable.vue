<script setup>
import { ref, computed } from 'vue';

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
    },
    scrollHeight: {
        type: String,
        default: '400px'
    },
    selection: {
        type: Array,
        default: () => []
    },
    selectionMode: {
        type: String,
        default: 'single'
    },
    dataKey: {
        type: String,
        default: 'id'
    }
});

const emit = defineEmits(['update:selection']);

// 부모 컴포넌트의 selection과 연결
const selected = computed({
    get() {
        return props.selection;
    },
    set(value) {
        console.log('BasicTable - 선택 변경:', value);
        emit('update:selection', value);
    }
});

const getAlignClass = (align) => {
    if (align === 'center') return 'text-center';
    if (align === 'right') return 'text-right';
    return 'text-left'; // 기본값
};
</script>

<template>
    <div class="border mt-10 p-6 border-gray-200 rounded-lg bg-white" :style="{ height: props.height }">
        <h2 class="text-xl font-semibold mb-4">{{ props.title }}</h2>
    <div>
        <DataTable 
            :value="data" 
            :tableStyle="{ minWidth: '50rem' }" 
            showGridlines  
            scrollable  
            :scrollHeight="scrollHeight" 
            responsiveLayout="scroll" 
            v-model:selection="selected" 
            :dataKey="dataKey" 
            :selectionMode="selectionMode"
            size="large">
            
            <Column v-if="selectionMode === 'multiple'" selectionMode="multiple" headerStyle="width: 3rem"></Column>
            <Column v-else-if="selectionMode === 'single'" selectionMode="single" headerStyle="width: 3rem"></Column>
            
            <Column v-for="col in columns" :key="col.field" :header="col.header" :headerClass="getAlignClass(col.align)" :bodyClass="getAlignClass(col.align)">
                <template #body="slotProps">
                    <template v-if="col.type === 'input'">
                        <div class="flex items-center border rounded w-full h-10">
                            <input
                                v-model="slotProps.data[col.field]"
                                :type="col.inputType || 'text'"
                                :readonly="col.readonly"
                                :disabled="col.disabled"
                                :class="['border-none outline-none flex-1 bg-transparent px-3 py-2', getAlignClass(col.align)]"
                            />
                            <i
                                v-if="col.suffixIcon"
                                :class="[col.suffixIcon, 'cursor-pointer text-gray-400 px-3 py-2']"
                                @click="$emit(col.suffixEvent, slotProps.data)"
                            />
                        </div>
                    </template>
                    <template v-else>
                        {{ slotProps.data[col.field] }}
                    </template>
                </template>
            </Column>
        </DataTable>
    </div>
    </div>
</template>