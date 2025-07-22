<script setup>
import { ref, computed, watch, h } from 'vue';
import InputText from 'primevue/inputtext';
import RadioButton from 'primevue/radiobutton';
import Calendar from 'primevue/calendar'; // 렌더 함수에서 사용
import Button from 'primevue/button';
import Fluid from 'primevue/fluid';

// 사용 방법
// { 
//     key: 'name', 
//     label: '이름', 
//     type: 'text', 
//     placeholder: '이름을 입력하세요' 
// },
// { 
//     key: 'status', 
//     label: '상태', 
//     type: 'radio', 
//     options: [
//         { label: '활성', value: 'active' },
//         { label: '비활성', value: 'inactive' }
//     ]
// },
//
// { 
//     key: 'dateRange', 
//     label: '등록일 범위', 
//     type: 'dateRange',
//     startPlaceholder: '시작일을 선택하세요',
//     endPlaceholder: '종료일을 선택하세요'
// },
//
// { 
//     key: 'balanceRange', 
//     label: '잔액 범위', 
//     type: 'numberRange',
//     minPlaceholder: '최소 잔액',
//     maxPlaceholder: '최대 잔액'
// },
// { 
//     key: 'singleDate', 
//     label: '특정일', 
//     type: 'calendar', 
//     placeholder: '날짜를 선택하세요' 
// }

// // Props 정의
// const props = defineProps({
//     columns: {
//         type: Array,
//         required: true
//     }
// });

// Emits 정의
const emit = defineEmits(['search', 'reset']);

// 검색 컬럼 상태 관리
const searchColumns = ref([]);

// Props의 columns를 기반으로 searchColumns 초기화
const initializeColumns = () => {
    searchColumns.value = props.columns.map(column => {
        let initialValue = '';
        
        // 범위 검색 타입들은 객체로 초기화
        if (column.type === 'dateRange') {
            initialValue = { start: null, end: null };
        } else if (column.type === 'numberRange') {
            initialValue = { min: null, max: null };
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

// 렌더 함수들 - 각 타입별로 렌더링 로직
const renderFunctions = {
    text: (column) => h(InputText, {
        id: `search_${column.key}`,
        modelValue: column.value,
        'onUpdate:modelValue': (value) => column.value = value,
        placeholder: column.placeholder,
        class: 'w-full'
    }),
    
    calendar: (column) => h(Calendar, {
        id: `search_${column.key}`,
        modelValue: column.value,
        'onUpdate:modelValue': (value) => column.value = value,
        placeholder: column.placeholder,
        dateFormat: 'yy-mm-dd',
        class: 'w-full',
        showIcon: true
    }),
    
    // 날짜 범위 검색 - 시작일~종료일
    dateRange: (column) => {
        // value가 객체가 아니면 초기화
        if (!column.value || typeof column.value !== 'object') {
            column.value = { start: null, end: null };
        }
        
        return h('div', { class: 'flex gap-2 items-center w-full' }, [
            h(Calendar, {
                modelValue: column.value.start,
                'onUpdate:modelValue': (value) => column.value.start = value,
                placeholder: column.startPlaceholder || '시작일',
                dateFormat: 'yy-mm-dd',
                class: 'flex-1',
                showIcon: true
            }),
            h('span', { class: 'text-gray-500 font-medium px-2' }, '~'),
            h(Calendar, {
                modelValue: column.value.end,
                'onUpdate:modelValue': (value) => column.value.end = value,
                placeholder: column.endPlaceholder || '종료일',
                dateFormat: 'yy-mm-dd',
                class: 'flex-1',
                showIcon: true
            })
        ]);
    },
    
    // 숫자 범위 검색 - 최소~최대
    numberRange: (column) => {
        // value가 객체가 아니면 초기화
        if (!column.value || typeof column.value !== 'object') {
            column.value = { min: null, max: null };
        }
        
        return h('div', { class: 'flex gap-2 items-center w-full' }, [
            h(InputText, {
                modelValue: column.value.min,
                'onUpdate:modelValue': (value) => column.value.min = value,
                placeholder: column.minPlaceholder || '최소값',
                type: 'number',
                class: 'flex-1'
            }),
            h('span', { class: 'text-gray-500 font-medium px-2' }, '~'),
            h(InputText, {
                modelValue: column.value.max,
                'onUpdate:modelValue': (value) => column.value.max = value,
                placeholder: column.maxPlaceholder || '최대값',
                type: 'number',
                class: 'flex-1'
            })
        ]);
    },
    
    radio: (column) => h('div', { class: 'flex gap-4' }, 
        column.options.map(option => 
            h('div', { class: 'flex items-center', key: option.value }, [
                h(RadioButton, {
                    id: `${column.key}_${option.value}`,
                    modelValue: column.value,
                    'onUpdate:modelValue': (value) => column.value = value,
                    value: option.value,
                    name: column.key
                }),
                h('label', {
                    for: `${column.key}_${option.value}`,
                    class: 'ml-2'
                }, option.label)
            ])
        )
    )
};

// 컴포넌트 렌더링 함수
const renderFieldComponent = (column) => {
    const renderFn = renderFunctions[column.type] || renderFunctions.text;
    return renderFn(column);
};

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
            column.value = { min: null, max: null };
        } else {
            column.value = '';
        }
    });
    emit('reset');
};
</script>

<template>
    <Fluid>
        <div class="flex flex-col gap-8">
            <!-- 검색 폼 영역 -->
            <div class="card flex flex-col gap-4">
                <div class="font-semibold text-xl">검색 조건</div>
                
                <!-- 동적 검색 필드들 -->
                <div 
                    v-for="(column, index) in searchColumns" 
                    :key="column.key"
                    class="grid grid-cols-12 gap-2 items-center"
                >
                    <label 
                        :for="`search_${column.key}`" 
                        class="flex items-center col-span-12 mb-2 md:col-span-2 md:mb-0"
                    >
                        {{ column.label }}
                    </label>
                    <div class="col-span-12 md:col-span-10">
                        <!-- 렌더 함수로 동적 컴포넌트 생성 -->
                        <component :is="() => renderFieldComponent(column)" />
                    </div>
                </div>
                
                <div class="flex gap-2 justify-end">
                    <Button label="초기화" severity="secondary" @click="handleReset" />
                    <Button label="검색" @click="handleSearch" />
                </div>
            </div>
        </div>
    </Fluid>
</template>