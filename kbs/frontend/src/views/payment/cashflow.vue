<script setup>
import { ref, onBeforeMount, onMounted, computed } from 'vue';
import { format } from 'date-fns';
import { useCommonStore } from '@/stores/commonStore';
import { useCashflowStore } from '@/stores/cashflowStore';
import { storeToRefs } from 'pinia';
import { useMemberStore } from '@/stores/memberStore';
import { useToast } from 'primevue/usetoast';
import SearchForm from '@/components/kimbap/searchform/SearchForm.vue';
import InputForm from '@/components/kimbap/searchform/inputForm.vue';
import StandardTable from '@/components/kimbap/table/StandardTable.vue';

// Pinia Store 상태 및 함수 바인딩
const store = useCashflowStore();
const { cashflowList, formData } = storeToRefs(store);
const { fetchCashflows, saveCashflow, fetchCashflowDetail } = store;
const memberStore = useMemberStore();
const { user } = storeToRefs(memberStore);
const toast = useToast();

const isEmployee = computed(() => user.value?.memType === 'p1');
const isManager = computed(() => user.value?.memType === 'p4');
const isAdmin = computed(() => user.value?.memType === 'p5');

// 오늘 날짜 포맷 (등록일자 default 값에 사용)
const today = format(new Date(), 'yyyy-MM-dd');

// 공통코드 가져오기
const common = useCommonStore();
const { commonCodes } = storeToRefs(common);

// 공통코드 형변환
const convertUnitCodes = (list) => {
    const transTypeCodes = common.getCodes('0U'); // 거래유형
    const calStatusCodes = common.getCodes('0X'); // 거래유형
    return list.map((item) => {
        const matchedtransType = transTypeCodes.find((code) => code.dcd === item.transType);
        const matchedcalStatusType = calStatusCodes.find((code) => code.dcd === item.calStatus);
        const formattedRegDt = item.regDt ? format(new Date(item.regDt), 'yyyy-MM-dd') : '';

        return {
            ...item,
            transType: matchedtransType ? matchedtransType.cdInfo : item.transType,
            calStatus: matchedcalStatusType ? matchedcalStatusType.cdInfo : item.calStatus,
            regDt: formattedRegDt,
            depositAmount: item.depositAmount?.toLocaleString() // 💥 여기!
        };
    });
};
const convertedcashflowList = computed(() => convertUnitCodes(cashflowList.value));

// UI 상태 정의
const searchColumns = ref([]); // 검색 컬럼
const inputColumns = ref([]); // 입력 폼 컬럼
const cashflowColumns = ref([]); // 입출금목록 테이블 컬럼
const inputFormButtons = ref({}); // 입출금 등록 버튼

// UI 구성 정의
onBeforeMount(() => {
    searchColumns.value = [
        { key: 'statementCd', label: '입출금코드', type: 'text', placeholder: '입출금코드를 입력하세요' },
        {
            key: 'transType',
            label: '거래유형',
            type: 'dropdown',
            options: [
                { label: '입금', value: 'u1' },
                { label: '출금', value: 'u2' }
            ]
        },
        {
            key: 'calStatus',
            label: '정산상태',
            type: 'dropdown',
            options: [
                { label: '미정산', value: 'x1' },
                { label: '정산 완료', value: 'x2' }
            ]
        },
        { key: 'regDt', label: '입금자명', type: 'dateRange' }
    ];
    inputColumns.value = [
        { key: 'statementCd', label: '입출금코드', type: 'readonly' },
        {
            key: 'transType',
            label: '거래유형',
            type: 'dropdown',
            options: [
                { label: '입금', value: 'u1' },
                { label: '출금', value: 'u2' }
            ]
        },
        { key: 'depo', label: '입금자명', type: 'text' },
        { key: 'depositAmount', label: '금액(원)', type: 'number' },
        {
            key: 'bankName',
            label: '은행',
            type: 'dropdown',
            options: [
                { label: '국민은행', value: '국민은행' },
                { label: '하나은행', value: '하나은행' },
                { label: '농협은행', value: '농협은행' }
            ]
        },
        { key: 'regDt', label: '등록일자', type: 'readonly', defaultValue: today },
        { key: 'note', label: '비고', type: 'textarea', rows: 1, cols: 20 }
    ];
    (cashflowColumns.value = [
        { field: 'statementCd', header: '입출금코드' },
        { field: 'transType', header: '거래유형' },
        { field: 'depositAmount', header: '금액(원)' },
        { field: 'regDt', header: '등록일자' },
        { field: 'calStatus', header: '정산 상태' }
    ]),
        (inputFormButtons.value = {
            save: { show: true, label: '저장', severity: 'success' }
        });
});

onMounted(async () => {
    await common.fetchCommonCodes('0U'); // 거래처 유형
    await common.fetchCommonCodes('0X'); // 거래처 유형
    await fetchCashflows();
});

// 거래처기준정보 등록 처리
const handleSaveCashflow = async () => {
    if (!user.value?.empCd) {
        toast.add({
            severity: 'warn',
            summary: '경고',
            detail: '로그인 정보가 없습니다.',
            life: 3000
        });
        return;
    }
    // 신규 등록이면 regi, 수정이면 modi 설정
    if (!formData.value.statementCd) {
        formData.value.regi = user.value.empCd;
    } else {
        formData.value.modi = user.value.empCd;
    }
    const result = await saveCashflow();
    if (result === '등록 성공') {
        toast.add({
            severity: 'success',
            summary: '등록 완료',
            detail: '정상적으로 등록되었습니다.',
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
    await fetchCashflowDetail(selectedRow.statementCd);
};

const clearForm = () => {
    formData.value = {
        regDt: today
    };
};
const handleReset = async () => {
    await fetchCashflows(); // 전체 목록 다시 조회
    toast.add({
        severity: 'info',
        summary: '초기화 완료 ✨',
        detail: '검색 조건이 초기화되고 전체 목록을 조회했습니다.',
        life: 3000
    });
};

const handleSearch = async (searchData) => {
    const params = {
        statementCd: searchData.statementCd,
        transType: searchData.transType,
        calStatus: searchData.calStatus, // ✅ 추가!
        regDtStart: searchData.regDt?.start ? format(new Date(searchData.regDt.start), 'yyyy-MM-dd') : null,
        regDtEnd: searchData.regDt?.end ? format(new Date(searchData.regDt.end), 'yyyy-MM-dd') : null
    };

    console.log('📤 검색 파라미터:', params);

    try {
        await fetchCashflows(params);

        if (!cashflowList.value.length) {
            toast.add({
                severity: 'info',
                summary: '검색 결과 없음',
                detail: '조건에 해당하는 입출금 내역이 없습니다.',
                life: 3000
            });
        } else {
            toast.add({
                severity: 'success',
                summary: '검색 성공',
                detail: `총 ${cashflowList.value.length}건의 결과가 조회되었습니다.`,
                life: 3000
            });
        }
    } catch (error) {
        toast.add({
            severity: 'error',
            summary: '검색 실패',
            detail: '입출금 내역 조회 중 오류가 발생했습니다.',
            life: 3000
        });
        console.error('❌ 검색 오류:', error);
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
                title="입출금 기준정보 목록"
                :data="convertedcashflowList"
                dataKey="statementCd"
                :columns="cashflowColumns"
                @row-select="handleSelectCompany"
                @clear-selection="clearForm"
                :showHistoryButton="false"
                :scrollable="true"
                :showRowCount="true"
                scrollHeight="420px"
                height="500px"
            />
        </div>
        <div class="w-full md:basis-[45%]">
            <InputForm title="입출금정보" :columns="inputColumns" v-model:data="formData" :buttons="inputFormButtons" @submit="handleSaveCashflow" />
        </div>
    </div>
</template>
