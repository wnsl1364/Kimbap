<script setup>
import { ref, computed, watch } from 'vue';

// Props 정의
const props = defineProps({
    columns: {
        type: Array,
        default: () => [
            {
                key: 'name',
                label: '이름',
                type: 'text',
                placeholder: '이름을 입력하세요'
            },
            {
                key: 'email',
                label: '이메일',
                type: 'text',
                placeholder: '이메일을 입력하세요'
            }
        ]
    },
    allowDynamicColumns: {
        type: Boolean,
        default: true
    }
});

// Emits 정의
const emit = defineEmits(['search', 'reset']);

// 검색 컬럼 상태 관리
const searchColumns = ref([]);

// Props의 columns를 기반으로 searchColumns 초기화
const initializeColumns = () => {
    searchColumns.value = props.columns.map(column => ({
        ...column,
        value: ''
    }));
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
    const searchData = searchData.value;
    console.log('검색 실행:', searchData);
    emit('search', searchData);
};

const handleReset = () => {
    searchColumns.value.forEach(column => {
        column.value = '';
    });
    emit('reset');
};

// 컬럼 추가 함수 (동적 컬럼이 허용된 경우에만)
const addColumn = () => {
    if (!props.allowDynamicColumns) return;
    
    const newKey = `field_${Date.now()}`;
    searchColumns.value.push({
        key: newKey,
        label: '새 필드',
        type: 'text',
        placeholder: '값을 입력하세요',
        value: ''
    });
};

// 컬럼 제거 함수 (동적 컬럼이 허용된 경우에만)
const removeColumn = (index) => {
    if (!props.allowDynamicColumns) return;
    if (searchColumns.value.length > 1) {
        searchColumns.value.splice(index, 1);
    }
};
</script>

<template>
    <Fluid>
        <div class="flex flex-col gap-8">
            <!-- 검색 폼 영역 -->
            <div class="card flex flex-col gap-4">
                <div class="flex justify-between items-center">
                    <div class="font-semibold text-xl">검색 조건</div>
                    <Button 
                        v-if="allowDynamicColumns"
                        label="컬럼 추가" 
                        icon="pi pi-plus" 
                        size="small" 
                        severity="secondary" 
                        @click="addColumn" 
                    />
                </div>
                
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
                    <div class="col-span-12 md:col-span-9">
                        <InputText 
                            :id="`search_${column.key}`" 
                            v-model="column.value" 
                            :type="column.type" 
                            :placeholder="column.placeholder" 
                        />
                    </div>
                    <div class="col-span-12 md:col-span-1 flex justify-center">
                        <Button 
                            v-if="allowDynamicColumns && searchColumns.length > 1"
                            icon="pi pi-trash" 
                            severity="danger" 
                            text 
                            size="small"
                            @click="removeColumn(index)"
                            class="p-button-rounded"
                        />
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
