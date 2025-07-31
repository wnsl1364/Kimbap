import { defineStore } from 'pinia';
import { ref } from 'vue';
import { format } from 'date-fns';
import { getCashflowList, insertCashflow, getCashflowDetail, updateCashflow} from '@/api/payment';

export const useCashflowStore = defineStore('cashflow', () => {
    // 전역 데이터 상태
    const cashflowList     = ref([]);    // 입출금 내역 목록
    const searchFilter     = ref({});    // 검색 필터
    const formData = ref({});          // 단건 조회

    // 빈문자열 처리함수
    function sanitizeFormData(obj) {
        const result = { ...obj };
        for (const key in result) {
        if (result[key] === '') {
            result[key] = null;
        }
        }
        return result;
    };

    // 검색조건 필터
    const setSearchFilter = (filter) => {
    searchFilter.value = filter;
    };

    // 입출금 내역 목록 
    const fetchCashflows = async (searchParams = {}) => {
        try {
            const res = await getCashflowList(searchParams); // ✅ 검색 조건 전달
            cashflowList.value = res.data;
        } catch (err) {
            console.error('입출금 내역 목록 조회 실패:', err);
        }
    };

    // 입출금 내역 저장 (등록 또는 수정)
    const saveCashflow = async () => {
        const sanitized = sanitizeFormData(formData.value);

        try {
        let res;

        if (!sanitized.statementCd) {
        res = await insertCashflow(sanitized);
        } else {
        res = await updateCashflow(sanitized);
        }

        if (res?.status === 200 && res.data?.success) {
            await fetchCashflows(); // 목록 갱신
            formData.value = {};     // 폼 초기화
            return !sanitized.statementCd ? '등록 성공' : '수정 성공';
        } else {
            return !sanitized.statementCd ? '등록 실패' : '수정 실패';
        }
        } catch (err) {
        console.error('입출금 내역 저장 중 예외 발생:', err);
        return '예외 발생';
        }
    }

    // 입출금 내역 기준정보 단건조회
    const fetchCashflowDetail = async (statementCd) => {
        if (!statementCd) {
            console.warn('⚠️ statementCd가 비어있습니다. 단건조회 중단');
            return;
        }

        try {
            const { data } = await getCashflowDetail(statementCd);
            console.log('API 응답 :', data);

            if (data.cashflow?.regDt) {
                data.cashflow.regDt = format(new Date(data.cashflow.regDt), 'yyyy-MM-dd');
            }

            formData.value = data.cashflow;
        } catch (err) {
            console.error('입출금 내역 단건 조회 실패:', err);
        }
    };

    return {
        cashflowList,
        searchFilter,
        formData,
        setSearchFilter,
        fetchCashflows,
        saveCashflow,
        fetchCashflowDetail
    }
})