import { defineStore } from 'pinia';
import { ref } from 'vue';
import { format } from 'date-fns';
import { getInComeList, settleUnpaid } from '@/api/payment'
import { getSalesList } from '@/api/standard';

export const useUnpaidStore = defineStore('unpaid', () => {
    const formData = ref({
        statementCd: '',
        cpCd : '',
        cpName: '',
        bankName: '',
        depo: '',
        depositAmount: '',
        regDt: ''
    });
    // 

    const resetForm = () => {
        formData.value = {
            statementCd: '',
            cpCd : '',
            cpName: '',
            bankName: '',
            depo: '',
            depositAmount: '',
            regDt: ''
        };
    };
    const companyList = ref({}); // 거래처 목록
    const cashflowList = ref([]); // 입출금내역 목록 

    // 입출금 내역 목록 조회
    const fetchCashflows = async () => {
        try {
            console.log('📤 입출금 API 호출 시작');
            const result = await getInComeList();
            console.log('📥 응답 데이터:', result.data);

            // 날짜 가공해서 한 번에 세팅
            cashflowList.value = result.data.map(item => ({
                ...item,
                regDt: item.regDt ? format(new Date(item.regDt), 'yyyy-MM-dd') : ''
            }));
        } catch (err) {
            console.error('❌ 입출금 목록 조회 실패:', err);
        }
    };

    // 거래처 목록 조회
    const fetchCompanys = async () => {
        try {
            const res = await getSalesList();
            companyList.value = res.data;
        } catch (err) {
            console.error('거래처 조회 실패 :', err);
        }
    }

    // 미정산 정산 처리 API 호출
    const fetchSave = async () => {
        try {
            const payload = {
                statementCd: formData.value.statementCd,
                cpCd: formData.value.cpCd, // cpCd는 formData에 없으니 외부에서 먼저 넣어줘야 함
                depositAmount: formData.value.depositAmount
            };

            if (!payload.statementCd || !payload.cpCd || !payload.depositAmount) {
                throw new Error('정산에 필요한 데이터가 부족합니다.');
            }

            await settleUnpaid(payload);
            alert('✅ 정산 완료');
            await fetchCompanys(); // 최신 데이터 반영
            resetForm();
        } catch (err) {
            console.error('❌ 정산 처리 실패:', err);
            alert('정산 중 오류 발생');
        }
    };


    return {
        formData,
        companyList,
        cashflowList,
        resetForm,
        fetchCompanys,
        fetchCashflows,
        fetchSave
    }
});
