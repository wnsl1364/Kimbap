<script setup>
import { ref, onMounted, onBeforeMount, computed } from 'vue';
import { useRoute } from 'vue-router';
import { format } from 'date-fns';
import { useToast } from 'primevue/usetoast';
import { storeToRefs } from 'pinia';
import { useCommonStore } from '@/stores/commonStore';
import { useRelsaveStore } from '@/stores/relsaveStore';
import LeftAlignTable from '@/components/kimbap/table/LeftAlignTable.vue';
import InputTable from '@/components/kimbap/table/InputTable.vue';
import Singleselect from '@/components/kimbap/modal/singleselect.vue';

// 🟩 Pinia 상태 및 액션
const store = useRelsaveStore();
const { formData, releaseList, products, allocationRows } = storeToRefs(store);
const { fetchRelsaves, fetchRelDetails, autoDistributeAll, autoDistributeOne } = store;

const common = useCommonStore();
const { commonCodes } = storeToRefs(common);
const toast = useToast();
const today = format(new Date(), 'yyyy-MM-dd');

// 🟦 거래처 코드 변환
const convertCompanyCodes = (list) => {
    const cpTypeCodes = common.getCodes('0J');
    return list.map((item) => {
        const matchedCpType = cpTypeCodes.find((code) => code.dcd === item.cpType);
        return {
            ...item,
            cpType: matchedCpType ? matchedCpType.cdInfo : item.cpType
            // 💥 여기!
        };
    });
};

// 🟨 입력 필드 정의
const relsaveFields = ref([]);
const columns = ref([]);
const columns2 = ref([]);

// 🟧 버튼 정의
const LeftAlignButtons = {
    save: { show: true, label: '저장', severity: 'success' },
    reset: { show: true, label: '초기화', severity: 'secondary' },
    load: { show: true, label: '출고지시서 불러오기', severity: 'info' }
};
const inputFormButtons = ref([]);
const purchaseFormButtons = ref([]);

// 불로 오기 모달 상태 및 로딩
const isRelsaveDialogVisible = ref(false);

// "불러오기" 버튼
const handleLoadOrder = async () => {
    try {
        await fetchRelsaves();
        isRelsaveDialogVisible.value = true;
    } catch (err) {
        toast.add({
            severity: 'error',
            summary: '불러오기 실패',
            detail: '불러오기 중 오류가 발생했습니다.',
            life: 3000
        });
        console.error('불러오기 내역 로딩 오류 : ', err);
    }
};

// 모달에서 지시서 선택 시 → 상세 조회 한 방
const handleRelsaveSelected = async (item) => {
    await fetchRelDetails(item.relMasCd); // ✅ 여기 한 줄로 products 채워짐
    isRelsaveDialogVisible.value = false;
};

const modalDataSets = {};


const handleSave = () => {};
const handleApprove = () => {};
const handleReject = () => {};

onBeforeMount(() => {
    relsaveFields.value = [
        { label: '출고지시번호', field: 'relMasCd', type: 'text', disabled: true },
        { label: '지시일자', field: 'relDt', type: 'text', disabled: true }
    ];
    columns.value = [
        { field: 'prodName', header: '제품명', type: 'input', disabled: true },
        { field: 'ordQty', header: '요청수량', type: 'input', disabled: true },
        { field: 'relOrdQty', header: '지시수량', type: 'input', disabled: true },
        { field: 'lotNo', header: 'LOT번호', type: 'input', disabled: true }
    ];
    columns2.value = [
        { field: 'prodName', header: '제품명', type: 'input', disabled: true },
        { field: 'lotNo', header: 'LOT번호', type: 'input', disabled: true },
        { field: 'allocQty', header: '출고수량', type: 'number' },
        { field: 'remainQty', header: '잔여수량', type: 'input', disabled: true }
    ];
});
</script>

<template>
    <div class="space-y-4 mb-3">
        <LeftAlignTable
            v-model:data="formData"
            :fields="relsaveFields"
            :title="'출고 지시서'"
            :buttons="LeftAlignButtons"
            button-position="top"
            :modalDataSets="modalDataSets"
            :dataKey="'ordCd'"
            @save="handleSave"
            @load="handleLoadOrder"
            @reset="handleApprove"
            @delete="handleReject"
        />
    </div>
    <div class="space-y-4 mt-3">
        <InputTable :data="products" :columns="columns" :title="'출고 제품'" scrollHeight="250px" height="305px" :dataKey="'pcode'" :buttons="inputFormButtons" :enableRowActions="false" :enableSelection="false" />
    </div>
    <div class="space-y-4 mt-3">
        <div class="flex gap-2 justify-end mb-2">
            <Button label="전체 자동배분(FEFO)" @click="autoDistributeAll" />
        </div>
        <InputTable :data="allocationRows" :columns="columns2" :title="'LOT별 수량'" scrollHeight="250px" height="305px" :dataKey="'lotNo'" :enableRowActions="false" :enableSelection="false" />
    </div>

    <!-- 불러오기 모달 -->
    <Singleselect
        v-model:visible="isRelsaveDialogVisible"
        :items="releaseList"
        :item-key="'relMasCd'"
        :columns="[
            { field: 'relMasCd', header: '출고지시번호' },
            { field: 'cpName', header: '거래처명' },
            { field: 'prodName', header: '제품명' },
            { field: 'ordQty', header: '총수량' }
        ]"
        @update:modelValue="handleRelsaveSelected"
    />
</template>
