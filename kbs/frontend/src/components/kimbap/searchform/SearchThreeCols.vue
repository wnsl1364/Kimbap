<script setup>
import { ref, computed, watch } from 'vue';
import InputText from 'primevue/inputtext';
import RadioButton from 'primevue/radiobutton';
import Calendar from 'primevue/calendar';
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import Fluid from 'primevue/fluid';

// Props 정의
const props = defineProps({
    columns: {
        type: Array,
        required: true
    },
    searchData: {
        type: Object,
        default: () => ({})
    }
});

// Emits 정의
const emit = defineEmits(['search', 'reset', 'update:searchData']);

// 검색 컬럼 상태 관리
const searchColumns = ref([]);

// 숫자 범위 검증 메서드들
const handleMinChange = (column) => {
    const minVal = parseFloat(column.value.min);
    const maxVal = parseFloat(column.value.max);
    
    if (!isNaN(minVal) && !isNaN(maxVal) && minVal > maxVal) {
        column.value.max = column.value.min;
    }
};

const handleMaxChange = (column) => {
    const minVal = parseFloat(column.value.min);
    const maxVal = parseFloat(column.value.max);
    
    if (!isNaN(maxVal) && !isNaN(minVal) && maxVal < minVal) {
        column.value.min = column.value.max;
    }
};

const validateRange = (column) => {
    const minVal = parseFloat(column.value.min);
    const maxVal = parseFloat(column.value.max);
    
    if (!isNaN(minVal) && !isNaN(maxVal) && minVal > maxVal) {
        column.value.max = column.value.min;
    }
};

// 검색 데이터 업데이트 함수
const updateSearchData = () => {
    const data = {};
    searchColumns.value.forEach(column => {
        data[column.key] = column.value;
    });
    emit('update:searchData', data);
};

// Props의 columns를 기반으로 searchColumns 초기화
const initializeColumns = () => {
    searchColumns.value = props.columns.map(column => {
        let initialValue = '';

        // searchData에서 해당 키의 값이 있으면 사용
        if (props.searchData && props.searchData[column.key] !== undefined) {
            initialValue = props.searchData[column.key];
        }
        
        // 타입별 기본값 설정 (항상 올바른 구조 보장)
        if (column.type === 'dateRange') {
            if (!initialValue || typeof initialValue !== 'object' || Array.isArray(initialValue)) {
                initialValue = { start: null, end: null };
            } else {
                // 기존 값이 있어도 올바른 구조로 정규화
                initialValue = {
                    start: initialValue.start || null,
                    end: initialValue.end || null
                };
            }
        } else if (column.type === 'numberRange') {
            if (!initialValue || typeof initialValue !== 'object') {
                initialValue = { min: 0, max: 0 };
            }
        } else if (!initialValue) {
            initialValue = '';
        }

        return {
            ...column,
            value: initialValue
        };
    });
    
    // 초기화 후 부모 컴포넌트에 데이터 전달
    updateSearchData();
};

// 컴포넌트 마운트 시 및 props 변경 시 초기화
initializeColumns();
watch(() => props.columns, initializeColumns, { deep: true });
watch(() => props.searchData, () => {
    if (props.searchData) {
        searchColumns.value.forEach(column => {
            if (props.searchData[column.key] !== undefined) {
                let newValue = props.searchData[column.key];
                
                // dateRange 타입인 경우 안전하게 처리
                if (column.type === 'dateRange') {
                    if (!newValue || typeof newValue !== 'object' || Array.isArray(newValue)) {
                        newValue = { start: null, end: null };
                    } else {
                        // 기존 값이 올바른 형태인지 확인
                        newValue = {
                            start: newValue.start || null,
                            end: newValue.end || null
                        };
                    }
                }
                
                column.value = newValue;
            }
        });
    }
}, { deep: true });

// 실시간 업데이트는 제거하고 검색 버튼 클릭 시에만 업데이트
// watch(searchColumns, () => {
//     updateSearchData();
// }, { deep: true });

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
    updateSearchData(); // 검색 버튼 클릭 시 데이터 업데이트
    emit('search', searchData.value);
};

const handleReset = () => {
    searchColumns.value.forEach(column => {
        if (column.type === 'dateRange') {
            column.value = { start: null, end: null }; // 시작일과 종료일 객체로 리셋
        } else if (column.type === 'numberRange') {
            column.value = { min: 0, max: 0 };
        } else {
            column.value = '';
        }
    });
    updateSearchData();
    emit('reset');
};
</script>

<template>
    <Fluid>
        <div class="flex flex-col border-2 border-black-600 gap-8">
            <!-- 검색 폼 영역 -->
            <div class="card flex flex-col gap-4 !p-5 !rounded-none">
                <!-- 동적 검색 필드들 - 3열 그리드 -->
                <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
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
                            
                            <!-- 읽기 전용 -->
                            <InputText v-else-if="column.type === 'readonly'" :id="`search_${column.key}`"
                                v-model="column.value" :placeholder="column.placeholder" class="w-full" readonly />

                            <!-- 날짜 범위 -->
                            <div v-else-if="column.type === 'dateRange'" class="flex gap-2 items-center w-full">
                                <Calendar v-if="column.value" v-model="column.value.start" :placeholder="column.startPlaceholder || '시작일'"
                                    dateFormat="yy-mm-dd" class="flex-1" showIcon />
                                <span class="text-gray-500 font-medium px-2">~</span>
                                <Calendar v-if="column.value" v-model="column.value.end" :placeholder="column.endPlaceholder || '종료일'"
                                    dateFormat="yy-mm-dd" class="flex-1" showIcon />
                            </div>

                            <!-- 드롭다운 -->
                            <Dropdown v-else-if="column.type === 'dropdown'" :id="`search_${column.key}`"
                                v-model="column.value" :options="column.options" :placeholder="column.placeholder"
                                optionLabel="label" optionValue="value" class="w-full" />

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
                            <div v-else-if="column.type === 'radio'" class="grid grid-cols-3 gap-2 w-full">
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
    background-color: #f8fafc !important;
    border-radius: 12px !important;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1) !important;
}

:deep(.p-fluid) {
    padding: 24px !important;
}

:deep(.p-fluid .p-component) {
    margin-bottom: 16px !important;
}
</style>