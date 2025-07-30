import { defineStore } from 'pinia';
import { ref } from 'vue';
import { format } from 'date-fns';
import { getWarehouseList, insertWarehouse, getWarehouseDetail, updateWarehouse, WhChangeHistory, getFactoryList } from '@/api/standard';

export const useStandardWhStore = defineStore('standardWh', () => {
    // 전역 데이터 상태
    const warehouseList     = ref([]);    // 창고 목록
    const factoryList      = ref([]);    // 공장 목록
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

    // 창고 목록 
    const fetchWarehouses = async () => {
        try {
            const res = await getWarehouseList();
            warehouseList.value = res.data;
        } catch (err) {
            console.error('창고 기준정보 목록 조회 실패:', err);
        }
    };

    // 창고 저장 (등록 또는 수정)
    const saveWarehouse = async () => {
        const sanitized = sanitizeFormData(formData.value);

        try {
        let res;

        if (!sanitized.wcode) {
        res = await insertWarehouse(sanitized);
        } else {
        res = await updateWarehouse(sanitized);
        }

        if (res?.status === 200 && res.data?.success) {
            await fetchWarehouses(); // 목록 갱신
            formData.value = {}; // 저장 후 폼 초기화
            return !sanitized.wcode ? '등록 성공' : '수정 성공';
        } else {
            return !sanitized.wcode ? '등록 실패' : '수정 실패';
        }
        } catch (err) {
        console.error('창고 저장 중 예외 발생:', err);
        return '예외 발생';
        }
    }
    // 창고 기준정보 단건조회
    const fetchWarehouseDetail = async (wcode) => {
        if (!wcode) {
            console.warn('⚠️ wcode가 비어있습니다. 단건조회 중단');
            return;
        }

        try {
            const { data } = await getWarehouseDetail(wcode);
            console.log('API 응답 :', data);

            if (data.warehouse?.regDt) {
                data.warehouse.regDt = format(new Date(data.warehouse.regDt), 'yyyy-MM-dd');
            }

            formData.value = data.warehouse;
        } catch (err) {
            console.error('창고 단건 조회 실패:', err);
        }
    };

    // 변경이력조회
    const fetchChangeHistory = async (wcode) => {
        try {
            const res = await WhChangeHistory(wcode);
            changeHistory.value = res.data.map(item => ({
                ...item,
                regDt: format(new Date(item.regDt), 'yyyy-MM-dd'),
            }));
        } catch (error){
            console.error('변경이력 조회 실패:', error);
        }
    };

    // 공장 목록 조회 함수
    const fetchFactoryList = async () => {
        try {
        const res = await getFactoryList();
        factoryList.value = res.data;
        } catch (err) {
        console.error('공장 목록 조회 실패:', err);
        }
    };

    return {
        warehouseList,
        factoryList,
        searchFilter,
        formData,
        changeHistory,
        fetchWarehouses,
        saveWarehouse,
        fetchWarehouseDetail,
        fetchChangeHistory,
        fetchFactoryList
    }
})