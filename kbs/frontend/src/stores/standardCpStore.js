import { defineStore } from 'pinia';
import { ref } from 'vue';
import { format } from 'date-fns';
import { getCompanyList, insertCompany, updateCompany, getCompanyDetail ,CpChangeHistory } from '@/api/standard';

export const useStandardCpStore = defineStore('standardCp', () => {
    // 전역 데이터 상태
    const companyList     = ref([]);    // 거래처 목록
    const searchFilter     = ref({});    // 검색 필터
    const formData = ref({});          // 단건 조회
    const changeHistory = ref([]);    // 변경 이력 조회

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
    
    // 거래처 목록 
    const fetchCompanys = async () => {
        try {
            const res = await getCompanyList();
            companyList.value = res.data;
        } catch (err) {
            console.error('거래처 기준정보 목록 조회 실패:', err);
        }
    };

    // 거래처 저장 (등록 또는 수정)
    const saveCompany = async () => {
        const sanitized = sanitizeFormData(formData.value);

        try {
        let res;

        if (!sanitized.cpCd) {
        res = await insertCompany(sanitized);
        } else {
        res = await updateCompany(sanitized);
        }

        if (res?.status === 200 && res.data?.success) {
            formData.value = {};
            await fetchCompanys(); // 목록 갱신
            return !sanitized.cpCd ? '등록 성공' : '수정 성공';
        } else {
            return !sanitized.cpCd ? '등록 실패' : '수정 실패';
        }
        } catch (err) {
        console.error('거래처 저장 중 예외 발생:', err);
        return '예외 발생';
        }
    }
    // 거래처 기준정보 단건조회
    const fetchCompanyDetail = async (cpCd) => {
        if (!cpCd) {
            console.warn('⚠️ cpCd가 비어있습니다. 단건조회 중단');
            return;
        }

        try {
            const { data } = await getCompanyDetail(cpCd);
            console.log('API 응답 :', data);

            if (data.company?.regDt) {
                data.company.regDt = format(new Date(data.company.regDt), 'yyyy-MM-dd');
            }

            formData.value = data.company;
        } catch (err) {
            console.error('거래처 단건 조회 실패:', err);
        }
    };

    // 변경이력조회
    const fetchChangeHistory = async (cpCd) => {
        try {
            const res = await CpChangeHistory(cpCd);
            changeHistory.value = res.data.map(item => ({
                ...item,
                regDt: format(new Date(item.regDt), 'yyyy-MM-dd'),
            }));
        } catch (error){
            console.error('변경이력 조회 실패:', error);
        }
    };

    return {
        companyList,
        searchFilter,
        formData,
        changeHistory,
        setSearchFilter,
        fetchCompanys,
        saveCompany,
        fetchCompanyDetail,
        fetchChangeHistory
    }
})