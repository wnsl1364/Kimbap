<script setup>
import { ref, watch } from 'vue';
import { defineProps, defineEmits } from 'vue';
import InputText from 'primevue/inputtext';
import Dropdown from 'primevue/dropdown';
import RadioButton from 'primevue/radiobutton';
import Button from 'primevue/button';
import Textarea from 'primevue/textarea';
import Calendar from 'primevue/calendar';

const props = defineProps({
    columns: {
        type: Array,
        required: true
    },
    title: { type: String, default: '' },
    // 새로 추가! 버튼 설정
    buttons: {
        type: Object,
        default: () => ({
            save: { show: true, label: '저장', severity: 'success' },
            reset: { show: true, label: '초기화', severity: 'secondary' },
            delete: { show: false, label: '삭제', severity: 'danger' },
            load: { show: false, label: '불러오기', severity: 'info' }
        })
    },
    // 버튼 위치 설정
    buttonPosition: {
        type: String,
        default: 'top', // 'top', 'bottom', 'both'
        validator: (value) => ['top', 'bottom', 'both'].includes(value)
    },
    data: { 
        type: Object, 
        required: true 
    },
    // 저장버튼 폼 초기화 비활성 기능
    autoResetOnSave: {
        type: Boolean,
        default: true // 기본값은 true (기존 동작 유지)
    }
});

const emit = defineEmits(['submit', 'reset', 'delete', 'load', 'update:data']);

const formData = ref({ ...props.data });

watch(
  () => props.data,
  (newVal) => {
    formData.value = { ...newVal };
  },
  { immediate: true, deep: true }
);

watch(
  () => formData.value,
  (newVal) => {
    const jsonNew = JSON.stringify(newVal);
    const jsonOld = JSON.stringify(props.data);
    if (jsonNew !== jsonOld) {
      emit('update:data', newVal);
    }
  },
  { deep: true }
);

// props.columns를 안전하게 초기화
const initializeForm = () => {
    if (!props.columns || !Array.isArray(props.columns)) return;
    formData.value = {};
    props.columns.forEach((field) => {
        formData.value[field.key] = field.defaultValue ?? '';
    });
};

// 초기화 실행
watch(
    () => props.columns,
    () => {
        initializeForm();
    },
    { immediate: true, deep: true }
);

// 이벤트 핸들러들
const handleSubmit = () => { 
    console.log('제출 데이터:', formData.value);
    emit('submit', formData.value);
    // autoResetOnSave prop에 따라 초기화 여부 결정
    if (props.autoResetOnSave) {
        initializeForm();
    }
};

const handleReset = () => {
    initializeForm();
    console.log('폼이 초기화되었습니다');
    emit('reset');
};

const handleDelete = () => {
    console.log('삭제 요청');
    emit('delete', formData.value);
};

const handleLoad = () => {
    console.log('불러오기 요청');
    emit('load');
};

const getOptions = (options) => {
  if (!options) return [];
  if (typeof options === 'function') return options();
  if ('value' in options) return options.value;
  return options;
};

const handleFieldClick = (field) => {
  if (field.clickable) {
    emit('fieldClick', field.key);
  }
};
</script>

<template>
    <div class="p-4 bg-orange-50">
        <div class="flex justify-between items-center">
            <h2 class="text-lg mb-0 font-semibold">{{ title }}</h2>
            <!-- 상단 버튼들 -->
            <div v-if="buttonPosition === 'top' || buttonPosition === 'both'" 
            class="flex justify-end gap-2 mb-4">
            <!-- 슬롯으로 커스텀 버튼 추가 가능 -->
            <slot name="top-buttons"></slot>
                
                <!-- 기본 버튼들 -->
                <Button 
                v-if="buttons.delete?.show" 
                :label="buttons.delete.label" 
                :severity="buttons.delete.severity"
                @click="handleDelete" 
                />
                <Button 
                v-if="buttons.reset?.show" 
                :label="buttons.reset.label" 
                :severity="buttons.reset.severity"
                @click="handleReset" 
                />
                <Button 
                v-if="buttons.save?.show" 
                :label="buttons.save.label" 
                :severity="buttons.save.severity"
                @click="handleSubmit" 
                />
                <Button 
                    v-if="buttons.load?.show" 
                    :label="buttons.load.label" 
                    :severity="buttons.load.severity"
                    @click="handleLoad" 
                />
            </div>
        </div>

        <!-- 폼 필드들 -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div v-for="field in props.columns" :key="field.key" class="flex flex-col gap-1">
                <label :for="field.key" class="font-medium text-gray-700">
                    {{ field.label }}
                    <span v-if="field.required" class="text-red-500 ml-1">*</span>
                </label>

                <!-- 텍스트 입력 -->
                <InputText 
                    v-if="field.type === 'text'" 
                    v-model="formData[field.key]" 
                    :placeholder="field.placeholder" 
                    :class="{ 'p-invalid': field.required && !formData[field.key] }"
                    :disabled="typeof field.disabled === 'function' ? field.disabled(formData) : field.disabled"
                    class="w-full" 
                />
                
                <Calendar 
                    v-else-if="field.type === 'calendar'" 
                    v-model="formData[field.key]" 
                    :placeholder="field.placeholder" 
                    :class="{ 'p-invalid': field.required && !formData[field.key] }"
                    :disabled="typeof field.disabled === 'function' ? field.disabled(formData) : field.disabled"
                    class="w-full" 
                />
                <Calendar 
                    v-else-if="field.type === 'calendar2'" 
                    v-model="formData[field.key]" 
                    :placeholder="field.placeholder" 
                    :class="{ 'p-invalid': field.required && !formData[field.key] }"
                    :disabled="typeof field.disabled === 'function' ? field.disabled(formData) : field.disabled"
                    class="w-full" 
                    dateFormat="yy-mm-dd"
                />

                <!-- 드롭다운 -->
                <Dropdown 
                v-else-if="field.type === 'dropdown'" 
                v-model="formData[field.key]" 
                :options="field.options" 
                optionLabel="label" 
                    optionValue="value" 
                    :placeholder="field.placeholder" 
                    :disabled="typeof field.disabled === 'function' ? field.disabled(formData) : field.disabled"
                    class="w-full"
                    />
                    <!-- 드롭다운 배열 반환 -->
                    <Dropdown 
                    v-else-if="field.type === 'dropdown2'" 
                    v-model="formData[field.key]" 
                    :options="getOptions(field.options)" 
                    optionLabel="label" 
                    optionValue="value" 
                    :placeholder="field.placeholder || '선택하세요'" 
                    class="w-full" 
                    />
                    
                    <!-- 라디오 버튼 -->
                    <div v-else-if="field.type === 'radio'" class="flex gap-4 items-center">
                        <div v-for="option in field.options" :key="option.value" class="flex items-center">
                        <RadioButton 
                        :id="`${field.key}_${option.value}`" 
                        v-model="formData[field.key]" 
                        :value="option.value" 
                        :name="field.key" 
                        />
                        <label :for="`${field.key}_${option.value}`" class="ml-2">
                            {{ option.label }}
                        </label>
                    </div>
                </div>
                
                <!-- 숫자 입력 -->
                <!-- 숫자 입력 (min/max/step 지원) -->
                <InputText 
                v-else-if="field.type === 'number'" 
                v-model="formData[field.key]" 
                :placeholder="field.placeholder" 
                :disabled="typeof field.disabled === 'function' ? field.disabled(formData) : field.disabled"
                type="number" 
                class="w-full"
                :min="field.min !== undefined ? field.min : null"
                :max="field.max !== undefined ? field.max : null"
                :step="field.step !== undefined ? field.step : null"
                />
                
                <!-- 읽기 전용 -->
                <InputText 
                v-else-if="field.type === 'readonly'" 
                v-model="formData[field.key]"
                :disabled="typeof field.disabled === 'function' ? field.disabled(formData) : field.disabled" 
                class="w-full bg-gray-100" 
                readonly 
                />

                <!-- 읽기 전용 모달 -->
                <div v-else-if="field.type === 'readonlyModal'" class="relative">
                    <InputText
                        v-model="formData[field.key]"
                        :placeholder="field.placeholder" 
                        :disabled="typeof field.disabled === 'function' ? field.disabled(formData) : field.disabled"
                        class="w-full bg-gray-100 cursor-pointer"
                        readonly
                        @click="() => handleFieldClick(field)"
                    />
                </div>
                
                <!-- 비활성화 -->
                <InputText 
                v-else-if="field.type === 'disabled'" 
                v-model="formData[field.key]" 
                :disabled="typeof field.disabled === 'function' ? field.disabled(formData) : field.disabled"
                class="w-full" 
                disabled 
                />
                
                <!-- textarea -->
                <Textarea 
                v-else-if="field.type === 'textarea'" 
                v-model="formData[field.key]" 
                :placeholder="field.placeholder" 
                :disabled="typeof field.disabled === 'function' ? field.disabled(formData) : field.disabled"
                :rows="field.rows || 3" 
                :cols="field.cols || 40" 
                class="w-full" 
                />

                
                <!-- 기본값 -->
                <InputText v-else v-model="formData[field.key]" class="w-full" />
            </div>
        </div>
        
        <!-- 하단 버튼들 -->
        <div v-if="buttonPosition === 'bottom' || buttonPosition === 'both'" 
        class="flex justify-end gap-2 mt-4">
        <!-- 슬롯으로 커스텀 버튼 추가 가능 -->
        <slot name="bottom-buttons"></slot>
        
        <!-- 기본 버튼들 -->
        <Button 
        v-if="buttons.load?.show" 
        :label="buttons.load.label" 
                :severity="buttons.load.severity"
                @click="handleLoad" 
            />
            <Button 
                v-if="buttons.delete?.show" 
                :label="buttons.delete.label" 
                :severity="buttons.delete.severity"
                @click="handleDelete" 
            />
            <Button 
                v-if="buttons.reset?.show" 
                :label="buttons.reset.label" 
                :severity="buttons.reset.severity"
                @click="handleReset" 
            />
            <Button 
                v-if="buttons.save?.show" 
                :label="buttons.save.label" 
                :severity="buttons.save.severity"
                @click="handleSubmit" 
            />
        </div>
    </div>
</template>