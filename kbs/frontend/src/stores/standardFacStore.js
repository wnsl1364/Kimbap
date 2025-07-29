import {
    defineStore
} from 'pinia';
import {
    ref
} from 'vue';
import {
    format
} from 'date-fns';
import {
    getFactoryList,
    getProductList,
    insertFactory,
    updateFactory,
    getFactoryDetail,
    FacChangeHistory
} from '@/api/standard';

export const useStandardFacStore = defineStore('standardFac', () => {
    // 전역 데이터 상태
    const factoryList = ref([]); // 자재 목록
    const facMaxList = ref([]); // 최대 생산량 목록
    const searchFilter = ref({}); // 검색 필터
    const formData = ref({}); // 단건 조회
    const changeHistory = ref([]); // 변경 이력 조회
    const facMaxData = ref([]); // 공장 최대생산량  정보

    // 빈문자열 처리함수
    function sanitizeFormData(obj) {
        const result = {
            ...obj
        };
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

    // 공장 목록 
    const fetchFactorys = async () => {
        try {
            const res = await getFactoryList();

            factoryList.value = res.data.map(item => ({
                ...item,
                regDt: item.regDt ? format(new Date(item.regDt), 'yyyy-MM-dd') : null
            }));
        } catch (err) {
            console.error('공장 기준정보 목록 조회 실패:', err);
        }
    };

    // 제품 목록 조회 
    const fetchFacMax = async () => {
        try {
            const res = await getProductList();
            facMaxList.value = res.data;
        } catch (err) {
            console.error('제품 조회 실패 :', err);
        }
    }

    // 공장 저장 (등록 또는 수정)
    const saveFactory = async () => {
        const sanitized = sanitizeFormData(formData.value);

        // ✅ mpqty 숫자 변환 처리
        sanitized.facMaxList = facMaxData.value.map((item) => ({
            pcode: item.pcode,
            mpqty: item.mpqty != null && item.mpqty !== '' && !isNaN(Number(item.mpqty)) ?
                Number(item.mpqty) :
                null
        }));

        console.log('✅ 전송 데이터:', JSON.stringify(sanitized));

        try {
            let res;
            if (!sanitized.fcode) {
                res = await insertFactory(sanitized);
            } else {
                res = await updateFactory(sanitized);
            }

            // ✅ optional chaining 올바르게 사용
            if (res ?.status === 200 && res.data ?.success) {
                facMaxData.value = [];

                await fetchFactorys();
                return !sanitized.fcode ? '등록 성공' : '수정 성공';
            } else {
                return !sanitized.fcode ? '등록 실패' : '수정 실패';
            }
        } catch (err) {
            console.error('공장 저장 중 예외 발생:', err);
            return '예외 발생';
        }
    };

    // 공장 기준정보 단건조회
    const fetchFactoryDetail = async (fcode) => {
        if (!fcode) {
            console.warn('⚠️ fcode가 비어있습니다. 단건조회 중단');
            return;
        }

        try {
            const {
                data
            } = await getFactoryDetail(fcode);
            console.log('API 응답 :', data);

            if (data.factory ?.regDt) {
                data.factory.regDt = format(new Date(data.factory.regDt), 'yyyy-MM-dd');
            }

            formData.value = data.factory;
            facMaxData.value = data.facMax;
        } catch (err) {
            console.error('공장 단건 조회 실패:', err);
        }
    };

    // 변경이력조회
    const fetchChangeHistory = async (fcode) => {
        try {
            const res = await FacChangeHistory(fcode);
            changeHistory.value = res.data.map(item => ({
                ...item,
                regDt: format(new Date(item.regDt), 'yyyy-MM-dd'),
            }));
        } catch (error) {
            console.error('변경이력 조회 실패:', error);
        }
    };

    return {
        factoryList,
        facMaxList,
        searchFilter,
        formData,
        changeHistory,
        facMaxData,
        setSearchFilter,
        fetchFactorys,
        fetchFacMax,
        saveFactory,
        fetchFactoryDetail,
        fetchChangeHistory
    }
})
