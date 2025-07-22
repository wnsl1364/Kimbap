<script setup>
import { ref, computed, watch, h } from 'vue';
import InputText from 'primevue/inputtext';
import RadioButton from 'primevue/radiobutton';
import Calendar from 'primevue/calendar';
import Button from 'primevue/button';
import Fluid from 'primevue/fluid';
import { defineProps, defineEmits } from 'vue';

// Props 정의
const props = defineProps({
    columns: {
        type: Array,
        required: true
    }
});

// Emits 정의
const emit = defineEmits(['search', 'reset']);

// 검색 컬럼 상태 관리
const searchColumns = ref([]);

// 숫자 범위 검증 메서드들 - const로 올바르게 정의! ✨
const handleMinChange = (column) => {
    const minVal = parseFloat(column.value.min);
    const maxVal = parseFloat(column.value.max);
    
    // min이 숫자이고, max가 있고, min이 max보다 크면
    if (!isNaN(minVal) && !isNaN(maxVal) && minVal > maxVal) {
        // max를 min과 같게 만들기
        column.value.max = column.value.min;
    }
};

const handleMaxChange = (column) => {
    const minVal = parseFloat(column.value.min);
    const maxVal = parseFloat(column.value.max);
    
    // max가 숫자이고, min이 있고, max가 min보다 작으면
    if (!isNaN(maxVal) && !isNaN(minVal) && maxVal < minVal) {
        // min을 max와 같게 만들기
        column.value.min = column.value.max;
    }
};

const validateRange = (column) => {
    const minVal = parseFloat(column.value.min);
    const maxVal = parseFloat(column.value.max);
    
    // 둘 다 숫자이고 min이 max보다 크면
    if (!isNaN(minVal) && !isNaN(maxVal) && minVal > maxVal) {
        // max를 min과 같게 조정
        column.value.max = column.value.min;
    }
};

// Props의 columns를 기반으로 searchColumns 초기화
const initializeColumns = () => {
    searchColumns.value = props.columns.map(column => {
        let initialValue = '';

        // 범위 검색 타입들은 객체로 초기화
        if (column.type === 'dateRange') {
            initialValue = { start: null, end: null };
        } else if (column.type === 'numberRange') {
            // 숫자 범위는 기본값 0으로 설정! ✨
            initialValue = { min: 0, max: 0 };
        }

        return {
            ...column,
            value: initialValue
        };
    });
};

// 컴포넌트 마운트 시 및 props 변경 시 초기화
initializeColumns();
watch(() => props.columns, initializeColumns, { deep: true });

// 검색 데이터를 객체로 변환
const searchData = computed(() => {
    const data = {};
    searchColumns.value.forEach(column => {
        data[column.key] = column.value;
    });
    return data;
});

const handleSearch = () => {
    console.log('검색 실행:', searchData.value);
    emit('search', searchData.value);
};

const handleReset = () => {
    searchColumns.value.forEach(column => {
        // 범위 검색 타입들은 객체로 리셋
        if (column.type === 'dateRange') {
            column.value = { start: null, end: null };
        } else if (column.type === 'numberRange') {
            // 숫자 범위는 기본값 0으로 리셋! ✨
            column.value = { min: 0, max: 0 };
        } else {
            column.value = '';
        }
    });
    emit('reset');
};
</script>

<template>
    <Fluid>
        <div class="flex flex-col border-2 border-black-600 gap-8">
            <!-- 검색 폼 영역 -->
            <div class="card flex flex-col gap-4 !rounded-none">

                <!-- 동적 검색 필드들 - 2열 그리드 -->
                <div class="grid grid-cols-1 lg:grid-cols-4 gap-6">
                    <div v-for="(column, index) in searchColumns" :key="column.key" class="flex flex-col gap-2">
                        <label :for="`search_${column.key}`" class="font-medium text-gray-700 mb-1">
                            {{ column.label }}
                        </label>
                        <div class="w-full">
                            <!-- 텍스트 입력 -->
                            <InputText v-if="column.type === 'text'" :id="`search_${column.key}`" v-model="column.value"
                                :placeholder="column.placeholder" class="w-full" />

                            <!-- 캘린더 -->
                            <Calendar v-else-if="column.type === 'calendar'" :id="`search_${column.key}`"
                                v-model="column.value" :placeholder="column.placeholder" dateFormat="yy-mm-dd"
                                class="w-full" showIcon />

                            <!-- 날짜 범위 -->
                            <div v-else-if="column.type === 'dateRange'" class="flex gap-2 items-center w-full">
                                <Calendar v-model="column.value.start" :placeholder="column.startPlaceholder || '시작일'"
                                    dateFormat="yy-mm-dd" class="flex-1" showIcon />
                                <span class="text-gray-500 font-medium px-2">~</span>
                                <Calendar v-model="column.value.end" :placeholder="column.endPlaceholder || '종료일'"
                                    dateFormat="yy-mm-dd" class="flex-1" showIcon />
                            </div>

                            <!-- 숫자 범위 -->
                            <div v-else-if="column.type === 'numberRange'" class="flex gap-2 items-center w-full">
                                <InputText v-model="column.value.min" :min="0" :step="column.step"
                                    :placeholder="column.minPlaceholder || '최소값'" type="number"
                                    class="flex-1" @input="handleMinChange(column)"
                                    @blur="validateRange(column)" />
                                <span class="text-gray-500 font-medium px-2">~</span>
                                <InputText v-model="column.value.max" :min="0" :step="column.step"
                                    :placeholder="column.maxPlaceholder || '최대값'" type="number"
                                    class="flex-1" @input="handleMaxChange(column)"
                                    @blur="validateRange(column)" />
                            </div>

                            <!-- 라디오 버튼 -->
                            <div v-else-if="column.type === 'radio'" class="grid grid-cols-2 gap-2 w-full">
                                <div v-for="option in column.options" :key="option.value"
                                    class="mt-1.5 flex items-center">
                                    <RadioButton :id="`${column.key}_${option.value}`" v-model="column.value"
                                        :value="option.value" :name="column.key" />
                                    <label :for="`${column.key}_${option.value}`" class="ml-2 text-lg">
                                        {{ option.label }}
                                    </label>
                                </div>
                            </div>

                            <!-- 기본 텍스트 (fallback) -->
                            <InputText v-else :id="`search_${column.key}`" v-model="column.value"
                                :placeholder="column.placeholder || '입력하세요'" class="w-full" />
                        </div>
                    </div>
                </div>

                <div class="flex gap-2 items-center justify-center">
                    <Button label="초기화" severity="secondary" class="!w-auto !min-w-40 !px-6" @click="handleReset" />
                    <Button label="검색" class="!w-auto !min-w-40 !px-6" @click="handleSearch" />
                </div>
            </div>
        </div>
    </Fluid>
</template>

<style scoped>
:deep(.custom-fluid) {
    /* PrimeVue가 강력해서 !important 필수야! */
    background-color: #f8fafc !important;
    border-radius: 12px !important;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1) !important;
}

/* 내부 컴포넌트들 */
:deep(.p-fluid) {
    padding: 24px !important;
}

:deep(.p-fluid .p-component) {
    margin-bottom: 16px !important;
}
</style>