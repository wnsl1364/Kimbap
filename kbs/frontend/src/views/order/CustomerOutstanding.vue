<script setup>
import { ref, onBeforeMount, onMounted, computed } from 'vue';
import { format } from 'date-fns';
import { storeToRefs } from 'pinia';
import { useCommonStore } from '@/stores/commonStore';
import { useCustomerOutstandingStore } from '@/stores/customeroutstandingStore';
import SearchForm from '@/components/kimbap/searchform/SearchForm.vue';
import InputTable from '@/components/kimbap/table/InputTable.vue';

// 🟩 Pinia 상태 및 액션
const store = useCustomerOutstandingStore();
const { customerList } = storeToRefs(store);
const { fetchCustomerOutstadings } = store;

// 공통코드 가져오기
const common = useCommonStore();
const { commonCodes } = storeToRefs(common);

// 공통코드 형변환
const convertUnitCodes = (list) => {
    return list.map((item) => {
        const formattedRegDt = item.regDt ? format(new Date(item.regDt), 'yyyy-MM-dd') : '';

        return {
            ...item,
            regDt: formattedRegDt,
            unsettledAmount: item.unsettledAmount?.toLocaleString() // 💥 여기!
        };
    });
};

const convertedcustomerList = computed(() => convertUnitCodes(customerList.value));

// UI 상태 정의
const searchColumns = ref([]); // 검색 컬럼
const InputTablecolumns = ref([]); // 목록 컬럼
const inputTableButtons = ref([]); // 인풋테이블 버튼

// UI 구성 정의
onBeforeMount(() => {
    searchColumns.value = [
        { key: 'cpCd', label: '거래처코드', type: 'text', placeholder: '거래처코드를 입력하세요' },
        { key: 'cpName', label: '거래처명', type: 'text', placeholder: '거래처명을 입력하세요' },
        { key: 'loanTerm', label: '여신기간(일)', type: 'number', placeholder: '여신기간을 입력하세요' }
    ];
    InputTablecolumns.value = [
        { field: 'cpCd', header: '거래처코드', type: 'readonly' },
        { field: 'cpName', header: '거래처명', type: 'readonly' },
        { field: 'crnumber', header: '사업자번호', type: 'readonly' },
        { field: 'loanTerm', header: '여신기간(일)', type: 'readonly' },
        { field: 'unsettledAmount', header: '미수금금액(원)', type: 'readonly', align: 'right' }
    ];
});

onMounted(async () => {
    await fetchCustomerOutstadings();
});

const handleSearch = async (searchData) => {
    await fetchCustomerOutstadings(); // 최신 데이터 가져오기
    customerList.value = customerList.value.filter((item) => {
        const matchcpCd = !searchData.cpCd || item.cpCd?.toLowerCase().includes(searchData.cpCd.toLowerCase());
        const matchcpName = !searchData.cpName || item.cpName?.includes(searchData.cpName);
        const matchloanTerm = !searchData.loanTerm || String(item.loanTerm) === String(searchData.loanTerm);
        return matchcpCd && matchcpName  && matchloanTerm;
    });
};

const handleReset = async () => {
    await fetchCustomerOutstadings();
};

// 총금액용
const totalOutstanding = computed(() => {
    return customerList.value
        .reduce((sum, item) => {
            const amount = Number(item.unsettledAmount ?? 0);
            return sum + amount;
        }, 0)
        .toLocaleString();
});

</script>

<template>
    <div class="space-y-4">
        <SearchForm :columns="searchColumns" @search="handleSearch" @reset="handleReset" :gridColumns="3" />
    </div>
    <div class="space-y-4 mt-8">
        <!-- 🔽 실제 테이블 -->
        <InputTable :columns="InputTablecolumns" :title="'미수금 내역'" :data="convertedcustomerList" scrollHeight="360px" height="460px" :enableSelection="false" :buttons="inputTableButtons" :enableRowActions="false" :showRowCount="true" />
        <!-- 🔽 총 미수금 금액 표시 -->
        <div class="flex justify-end items-center text-lg font-semibold text-gray-800 mb-2">
            총 미수금 금액:
            <span class="text-red-600 ml-2">{{ totalOutstanding }} 원</span>
        </div>
    </div>
</template>
