<script setup>
import { CustomerService } from '@/service/CustomerService';
import { onBeforeMount, ref } from 'vue';
import SearchForm from '@/components/kimbap/searchform/SearchForm.vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import ToggleButton from 'primevue/togglebutton';
import { defineProps, defineEmits } from 'vue';

const customers2 = ref(null);
const balanceFrozen = ref(false);

function formatCurrency(value) {
    return value.toLocaleString('en-US', { style: 'currency', currency: 'USD' });
}

// SearchForm props 정의
const props = defineProps({
    searchColumns: {
        type: Array,
        default: () => [
            { 
                key: 'name', 
                label: '이름', 
                type: 'text', 
                placeholder: '이름을 입력하세요' 
            },
            { 
                key: 'status', 
                label: '상태', 
                type: 'radio', 
                options: [
                    { label: '활성', value: 'active' },
                    { label: '비활성', value: 'inactive' }
                ]
            },
            // 숫자 범위 검색 예시  
            { 
                key: 'balanceRange', 
                label: '잔액 범위', 
                type: 'numberRange',
                step: 1000,
                minPlaceholder: '최소 잔액',
                maxPlaceholder: '최대 잔액'
            },
            // 날짜 범위 검색 예시
            { 
                key: 'dateRange', 
                label: '등록일 범위', 
                type: 'dateRange',
                startPlaceholder: '시작일을 선택하세요',
                endPlaceholder: '종료일을 선택하세요'
            },
            { 
                key: 'singleDate', 
                label: '특정일', 
                type: 'calendar', 
                placeholder: '날짜를 선택하세요' 
            }
        ]
    }
});

onBeforeMount(() => {
    CustomerService.getCustomersLarge().then((data) => (customers2.value = data));
});

// 검색 이벤트 핸들러
const handleSearch = (searchData) => {
    console.log('테이블 컴포넌트에서 받은 검색 데이터:', searchData);
    // 여기에 검색 로직 구현
    props.searchColumns.forEach(column => {
        column.value = searchData[column.key] || '';
    });
};

// 리셋 이벤트 핸들러
const handleReset = () => {
    console.log('검색 조건이 리셋되었습니다');
    // 여기에 리셋 로직 구현
    props.searchColumns.forEach(column => {
        column.value = '';
    });
};
</script>

<template>          
    <div class="flex flex-col gap-8">
        <!-- 검색 폼 컴포넌트 -->
        <SearchForm 
            :columns="searchColumns"
            @search="handleSearch"
            @reset="handleReset"
        />
        
        <!-- 테이블 영역 -->
        <div class="card">
            <div class="font-semibold text-xl mb-4">Frozen Columns</div>
            <ToggleButton v-model="balanceFrozen" onIcon="pi pi-lock" offIcon="pi pi-lock-open" onLabel="Balance" offLabel="Balance" />

            <DataTable :value="customers2" scrollable scrollHeight="400px" class="mt-6">
                <Column field="name" header="Name" style="min-width: 200px" frozen class="font-bold"></Column>
                <Column field="id" header="Id" style="min-width: 100px"></Column>
                <Column field="name" header="Name" style="min-width: 200px"></Column>
                <Column field="country.name" header="Country" style="min-width: 200px"></Column>
                <Column field="date" header="Date" style="min-width: 200px"></Column>
                <Column field="company" header="Company" style="min-width: 200px"></Column>
                <Column field="status" header="Status" style="min-width: 200px"></Column>
                <Column field="activity" header="Activity" style="min-width: 200px"></Column>
                <Column field="representative.name" header="Representative" style="min-width: 200px"></Column>
                <Column field="balance" header="Balance" style="min-width: 200px" alignFrozen="right" :frozen="balanceFrozen">
                    <template #body="{ data }">
                        <span class="font-bold">{{ formatCurrency(data.balance) }}</span>
                    </template>
                </Column>
            </DataTable>
        </div>
    </div>
</template>

<style scoped>

</style>