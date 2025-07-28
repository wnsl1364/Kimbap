import { defineStore } from 'pinia';
import { ref } from 'vue';
import { format } from 'date-fns';
import { getProductList,insertProduct, updateProdcut, getProductDetail, ProdChangeHistory } from '@/api/standard';

export const useStandardProdStore = defineStore('standardProd', () => {
    // 전역 데이터 상태
    const productList     = ref([]);    // 자재 목록
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

    // 제품 목록 
    const fetchProducts = async () => {
    try {
        const res = await getProductList();
        productList.value = res.data;
    } catch (err) {
        console.error('제품 기준정보 목록 조회 실패:', err);
    }
    };

    // 제품 저장 (등록 또는 수정)
    const saveProduct = async () => {
        const sanitized = sanitizeFormData(formData.value);

        try {
        let res;

        if (!sanitized.pcode) {
        res = await insertProduct(sanitized);
        } else {
        res = await updateProdcut(sanitized);
        }

        if (res?.status === 200 && res.data?.success) {
            await fetchProducts(); // 목록 갱신
            return !sanitized.pcode ? '등록 성공' : '수정 성공';
        } else {
            return !sanitized.pcode ? '등록 실패' : '수정 실패';
        }
        } catch (err) {
        console.error('제품 저장 중 예외 발생:', err);
        return '예외 발생';
        }
    }
    // 제품기준정보 단건조회
    const fetchProductDetail = async (pcode) => {
        try {
            const { data } = await getProductDetail(pcode);
            console.log('API 응답 :', data);

            // 등록일자 포멧처리
            if (data.product?.regDt) {
                data.product.regDt = format(new Date(data.product.regDt), 'yyyy-MM-dd');
            }
            formData.value = data.product;
        }catch (err){
            console.error('제품 단건 조회 실패:', err);
        }
    };
    // 변경이력조회
    const fetchChangeHistory = async (pcode) => {
        try {
            const res = await ProdChangeHistory(pcode);
            changeHistory.value = res.data.map(item => ({
                ...item,
                regDt: format(new Date(item.regDt), 'yyyy-MM-dd'),
            }));
        } catch (error){
            console.error('변경이력 조회 실패:', error);
        }
    };

    return {
        productList,
        searchFilter,
        formData,
        changeHistory,
        setSearchFilter,
        fetchProducts,
        saveProduct,
        fetchProductDetail,
        fetchChangeHistory
    }
})