<script setup>
import { ref, onBeforeMount, onMounted, computed } from 'vue';
import { format } from 'date-fns';
import { useCommonStore } from '@/stores/commonStore';
import { useStandardCpStore } from '@/stores/standardCpStore';
import { storeToRefs } from 'pinia';
import SearchForm from '@/components/kimbap/searchform/SearchForm.vue';
import InputForm from '@/components/kimbap/searchform/inputForm.vue';
import StandardTable from '@/components/kimbap/table/StandardTable.vue';

// Pinia Store 상태 및 함수 바인딩
const store = useStandardCpStore();
const { companyList, formData, changeHistory } = storeToRefs(store);
const { fetchCompanys, saveCompany, fetchCompanyDetail, fetchChangeHistory } = store;

// 오늘 날짜 포맷 (등록일자 default 값에 사용)
const today = format(new Date(), 'yyyy-MM-dd');

// 공통코드 가져오기
const common = useCommonStore();
const { commonCodes } = storeToRefs(common);

// 공통코드 형변환
const convertUnitCodes = (list) => {
    const cpTypeCodes = common.getCodes('0J'); // 거래처유형
    return list.map((item) => {
        const matchedcpType = cpTypeCodes.find((code) => code.dcd === item.cpType);
        return {
            ...item,
            cpType: matchedcpType ? matchedcpType.cdInfo : item.cpType
        };
    });
};
const convertedcompanyList = computed(() => convertUnitCodes(companyList.value));

// UI 상태 정의
const searchColumns = ref([]); // 검색 컬럼
const inputColumns = ref([]); // 입력 폼 컬럼
const productColumns = ref([]); // 거래처목록 테이블 컬럼
const inputFormButtons = ref({}); // 거래처 등록 버튼
  
// UI 구성 정의
onBeforeMount(() => {
    searchColumns.value = [
        { key: 'cpCd', label: '거래처코드', type: 'text', placeholder: '거래처코드를 입력하세요' },
        { key: 'cpName', label: '거래처명', type: 'text', placeholder: '거래처코드를 입력하세요' },
        {
            key: 'cpType',
            label: '거래처유형',
            type: 'dropdown',
            placeholder: '거래처유형을 입력하세요',
            options: [
                { label: '공급업체', value: 'j1' },
                { label: '매출업체', value: 'j2' }
            ]
        },
        { key: 'loanTerm', label: '여신기간(일)', type: 'text', placeholder: '여신기간을 입력하세요' }
    ];
    inputColumns.value = [
        { key: 'cpCd', label: '거래처코드', type: 'readonly' },
        { key: 'cpName', label: '거래처명', type: 'text' },
        {
            key: 'cpType',
            label: '거래처유형',
            type: 'dropdown',
            options: [
                { label: '공급업체', value: 'j1' },
                { label: '매출업체', value: 'j2' }
            ]
        },
        { key: 'repname', label: '대표자명', type: 'text' },
        { key: 'crnumber', label: '사업자번호', type: 'text' },
        { key: 'tel', label: '연락처(-포함)', type: 'text' },
        { key: 'cpEmail', label: '이메일', type: 'text' },
        { key: 'address', label: '주소', type: 'text' },
        { key: 'faxNum', label: '팩스번호', type: 'text' },
        { key: 'mname', label: '담당자명', type: 'text' },
        { key: 'loanTerm', label: '여신기간(일)', type: 'number' },
        { key: 'regDt', label: '등록일자', type: 'readonly', defaultValue: today },
        {
            key: 'isUsed',
            label: '사용여부',
            type: 'radio',
            options: [
                { label: '활성화', value: 'f1' },
                { label: '비활성화', value: 'f2' }
            ]
        },
        {
            key: 'chaRea',
            label: '변경사유',
            type: 'text',
            disabled: (row) => !row.cpCd
        },
        { key: 'note', label: '비고', type: 'textarea', rows: 1, cols: 20 }
    ];
    productColumns.value = [
        { field: 'cpCd', header: '거래처코드' },
        { field: 'cpName', header: '거래처명' },
        { field: 'cpType', header: '거래처유형' },
        { field: 'repname', header: '대표자명' },
        { field: 'loanTerm', header: '여신기간(일)' }
    ];

    inputFormButtons.value = {
        save: { show: true, label: '저장', severity: 'success' }
    };
});

onMounted(async () => {
    await common.fetchCommonCodes('0J'); // 거래처 유형

    await fetchCompanys();
});

// 거래처기준정보 등록 처리
const handleSaveCompany = async () => {
    const result = await saveCompany();
    alert(result === '등록 성공' ? '등록 성공' : result);
};

// 거래처 단건 조회 처리
const handleSelectCompany = async (selectedRow) => {
    await fetchCompanyDetail(selectedRow.cpCd);
};

const clearForm = () => {
    formData.value = {}; // 또는 필요한 초기화 방식으로
};

const handleReset = async () => {
    await fetchCompanys(); // 전체 목록 다시 조회
};

const handleSearch = async (searchData) => {
    await fetchCompanys(); // 최신 데이터 가져오기

    companyList.value = companyList.value.filter((item) => {
        const matchcpCd = !searchData.cpCd || item.cpCd?.toLowerCase().includes(searchData.cpCd.toLowerCase());
        const matchcpName = !searchData.cpName || item.cpName?.includes(searchData.cpName);
        const matchcpType = !searchData.cpType || item.cpType?.includes(searchData.cpType);
        const matchloanTerm = !searchData.loanTerm || String(item.loanTerm) === String(searchData.loanTerm);

        return matchcpCd && matchcpName && matchcpType && matchloanTerm;
    });
};
</script>
<template>
    <!-- 검색 영역 -->
    <SearchForm :columns="searchColumns" @search="handleSearch" @reset="handleReset" />

    <!-- 메인 영역 -->
    <div class="flex flex-col md:flex-row gap-4 mt-6">
        <div class="w-full md:basis-[55%]">
            <StandardTable
                title="거래처 기준정보 목록"
                :data="convertedcompanyList"
                dataKey="cpCd"
                :columns="productColumns"
                @row-select="handleSelectCompany"
                @clear-selection="clearForm"
                :showHistoryButton="false"
                :scrollable="true"
                scrollHeight="530px"
                height="630px"
            />
        </div>
        <div class="w-full md:basis-[45%]">
            <InputForm title="거래처정보" :columns="inputColumns" v-model:data="formData" :buttons="inputFormButtons" @submit="handleSaveCompany" />
        </div>
    </div>
</template>
