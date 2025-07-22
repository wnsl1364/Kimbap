<script setup>
import { ref, watch } from 'vue';
import InputText from 'primevue/inputtext';
import Dropdown from 'primevue/dropdown';
import RadioButton from 'primevue/radiobutton';
import Button from 'primevue/button';
import { defineProps, defineEmits } from 'vue';

const props = defineProps({
    columns: {
        type: Array,
        required: true
    }
});

const emit = defineEmits(['submit']);

const formData = ref({});

// props.columns를 안전하게 초기화
const initializeForm = () => {
    if (!props.columns || !Array.isArray(props.columns)) return;
    formData.value = {};
    props.columns.forEach((field) => {
        formData.value[field.key] = field.defaultValue ?? '';
    });
};

// 초기화 실행 (watch로도 감지)
watch(
    () => props.columns,
    () => {
        initializeForm();
    },
    { immediate: true, deep: true }
);

const handleSubmit = () => {
    console.log('제출 데이터:', formData.value);
    emit('submit', formData.value);
};
</script>

<template>
    <div class="p-4 bg-orange-50" >
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <!-- 저장 버튼 -->
            <div class="col-span-1 md:col-span-2 flex justify-end">
                <Button label="저장" @click="handleSubmit" />
            </div>
            <div v-for="field in props.columns" :key="field.key" class="flex flex-col gap-1">
                <label :for="field.key" class="font-medium text-gray-700">
                    {{ field.label }}
                </label>
                
                <!-- 텍스트 입력 -->
                <InputText v-if="field.type === 'text'" v-model="formData[field.key]" :placeholder="field.placeholder" class="w-full" />
                
                <!-- 드롭다운 -->
                <Dropdown v-else-if="field.type === 'dropdown'" v-model="formData[field.key]" :options="field.options" optionLabel="label" optionValue="value" :placeholder="field.placeholder" class="w-full" />
                
                <!-- 라디오 버튼 -->
                <div v-else-if="field.type === 'radio'" class="flex gap-4 items-center">
                    <div v-for="option in field.options" :key="option.value" class="flex items-center">
                        <RadioButton :id="`${field.key}_${option.value}`" v-model="formData[field.key]" :value="option.value" :name="field.key" />
                        <label :for="`${field.key}_${option.value}`" class="ml-2">
                            {{ option.label }}
                        </label>
                    </div>
                </div>
                
                <!-- 읽기 전용 -->
                <InputText v-else-if="field.type === 'readonly'" v-model="formData[field.key]" class="w-full" readonly />
                
                <!-- 기본값 (fallback) -->
                <InputText v-else v-model="formData[field.key]" class="w-full" />
            </div>
        </div>
    </div>
</template>
