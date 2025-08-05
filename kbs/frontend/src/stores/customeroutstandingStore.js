import { defineStore } from 'pinia';
import { ref } from 'vue';
import { format } from 'date-fns';
import { getCustomerList } from '@/api/standard';

export const useCustomerOutstandingStore = defineStore('CustomerOutstanding', () => {
    const searchFilter     = ref({}); // 검색필터
    const customerList     = ref([]); // 내역 목록
    const formData = ref({});       

    // 내역 목록 
    const fetchCustomerOutstadings = async () => {
        try {
            const res = await getCustomerList();
            customerList.value = res.data;
        } catch (err) {
            console.log("미수금 내역 조회 실패: ", err);
        }
    }
    return {
        customerList,
        searchFilter,
        formData,
        fetchCustomerOutstadings
    }
})