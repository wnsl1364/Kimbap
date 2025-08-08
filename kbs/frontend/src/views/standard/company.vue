<script setup>
import { ref, onBeforeMount, onMounted, computed } from 'vue';
import { format } from 'date-fns';
import { useCommonStore } from '@/stores/commonStore';
import { useStandardCpStore } from '@/stores/standardCpStore';
import { useMemberStore } from '@/stores/memberStore';
import { storeToRefs } from 'pinia';
import { useToast } from 'primevue/usetoast';
import SearchForm from '@/components/kimbap/searchform/SearchForm.vue';
import InputForm from '@/components/kimbap/searchform/inputForm.vue';
import StandardTable from '@/components/kimbap/table/StandardTable.vue';


// Pinia Store 상태 및 함수 바인딩
const store = useStandardCpStore();
const { companyList, formData, changeHistory } = storeToRefs(store);
const { fetchCompanys, saveCompany, fetchCompanyDetail } = store;
const memberStore = useMemberStore();
const { user } = storeToRefs(memberStore);
const toast = useToast();

const isEmployee = computed(() => user.value?.memType === 'p1');
const isManager = computed(() => user.value?.memType === 'p4');
const isAdmin = computed(() => user.value?.memType === 'p5');

console.log('현재 사용자 권한:', user.value);

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
const exportColumns = ref([]); 

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
        { key: 'loanTerm', label: '여신기간(일)', type: 'number',min: 0 },
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
        { field: 'loanTerm', header: '여신기간(일)', align: 'right', slot: true }
    ];

    inputFormButtons.value = {
        save: { show: isAdmin.value || isManager.value, label: '저장', severity: 'success' }
    };
    exportColumns.value = [
      { field: 'cpCd', header: '거래처코드' },
      { field: 'cpName', header: '거래처명' },
      { field: 'cpType', header: '거래처유형' },
      { field: 'crnumber', header: '사업자번호' },
      { field: 'repname', header: '대표자명' },
      { field: 'tel', header: '연락처(-포함)' },
      { field: 'mname', header: '담당자명' },
      { field: 'loanTerm', header: '여신기간(일)' },
    ]
});

onMounted(async () => {
    await common.fetchCommonCodes('0J'); // 거래처 유형

    await fetchCompanys();
});

// 거래처기준정보 등록 처리
const handleSaveCompany = async () => {
  if (!isAdmin.value && !isManager.value) {
    toast.add({
      severity: 'error',
      summary: '등록 실패',
      detail: '등록 권한이 없습니다.',
      life: 3000
    });
    return;
  }

  if (!user.value?.empCd) {
    toast.add({
      severity: 'warn',
      summary: '경고',
      detail: '로그인 정보가 없습니다.',
      life: 3000
    });
    return;
  }

  if (!formData.value.cpCd) {
    formData.value.regi = user.value.empCd;
  } else {
    formData.value.modi = user.value.empCd;
  }

  const result = await saveCompany();

  if (result === '등록 성공') {
    toast.add({
      severity: 'success',
      summary: '등록 완료',
      detail: '거래처가 정상적으로 등록되었습니다.',
      life: 3000
    });
  } else {
    toast.add({
      severity: 'error',
      summary: '등록 실패',
      detail: result,
      life: 3000
    });
  }
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
    toast.add({
        severity: 'info',
        summary: '초기화 완료 ✨',
        detail: '검색 조건이 초기화되고 전체 목록을 조회했습니다.',
        life: 3000
    });
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

  if (companyList.value.length === 0) {
    toast.add({
      severity: 'info',
      summary: '검색 결과 없음',
      detail: '조건에 해당하는 거래처가 없습니다.',
      life: 3000
    });
  } else {
    toast.add({
      severity: 'success',
      summary: '검색 성공',
      detail: `총 ${companyList.value.length}건의 거래처가 검색되었습니다.`,
      life: 3000
    });
  }
};
</script>
<template>
    <!-- 검색 영역 -->
    <SearchForm :columns="searchColumns" @search="handleSearch" @reset="handleReset" />

    <!-- 메인 영역 -->
    <div class="flex flex-col md:flex-row gap-4 mt-6">
        <div class="w-full md:basis-[55%]">
            <StandardTable
                title="거래처 목록"
                :data="convertedcompanyList"
                dataKey="cpCd"
                :columns="productColumns"
                @row-select="handleSelectCompany"
                @clear-selection="clearForm"
                :showHistoryButton="false"
                :scrollable="true"
                scrollHeight="530px"
                height="630px"
                :showRowCount="true"
                :showExcelDownload="true"
                :exportColumns="exportColumns"
            />
        </div>
        <div class="w-full md:basis-[45%]">
            <InputForm title="거래처정보" :columns="inputColumns" v-model:data="formData" :buttons="inputFormButtons" @submit="handleSaveCompany" />
        </div>
    </div>
</template>
