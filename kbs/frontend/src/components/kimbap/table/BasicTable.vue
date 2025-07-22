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

const getAlignClass = (align) => {
    if (align === 'center') return 'text-center';
    if (align === 'right') return 'text-right';
    return 'text-left'; // 기본값
};
</script>
<template>
    <div>
        <DataTable :value="data" :tableStyle="{ minWidth: '50rem' }" showGridlines :rows="10" responsiveLayout="scroll" v-model:selection="selected" dataKey="id" size="large">
            <Column selectionMode="multiple" headerStyle="width: 3rem"></Column>
            <Column v-for="col in columns" :key="col.field" :header="col.header" :headerClass="getAlignClass(col.align)" :bodyClass="getAlignClass(col.align)">
                <template #body="slotProps">
                    <template v-if="col.type === 'input'">
                        <div class="flex items-center border rounded w-full h-10">
                            <input
                                v-model="slotProps.data[col.field]"
                                :type="col.inputType || 'text'"
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
</template>