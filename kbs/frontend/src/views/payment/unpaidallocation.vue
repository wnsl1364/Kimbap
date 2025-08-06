<script setup>
import { ref, onBeforeMount, computed, onMounted, watch } from 'vue';
import { storeToRefs } from 'pinia';
import { useUnpaidStore } from '@/stores/unpaidStore';
import { useCommonStore } from '@/stores/commonStore';
import { useToast } from 'primevue/usetoast';
import LeftAlignTable from '@/components/kimbap/table/LeftAlignTable.vue';
import InputTable from '@/components/kimbap/table/InputTable.vue';
import Singleselect from '@/components/kimbap/modal/singleselect.vue';

// 🟩 Pinia 상태 및 액션
const store = useUnpaidStore();
const { formData, companyList, cashflowList } = storeToRefs(store);
const { fetchCompanys, fetchCashflows, resetForm, fetchSave } = store;

const common = useCommonStore();
const { commonCodes } = storeToRefs(common);
const toast = useToast();

// 🟦 거래처 코드 변환
const convertCompanyCodes = (list) => {
    const cpTypeCodes = common.getCodes('0J');
    return list.map((item) => {
        const matchedCpType = cpTypeCodes.find((code) => code.dcd === item.cpType);
        return {
            ...item,
            cpType: matchedCpType ? matchedCpType.cdInfo : item.cpType
        };
    });
};

// 🟨 입력 필드 정의
const unpaidFields = ref([]);
const columns = ref([]);

onBeforeMount(() => {
    unpaidFields.value = [
        { label: '입출금내역코드', field: 'statementCd', type: 'text', disabled: true },
        { label: '거래처명', field: 'cpName', type: 'modal', modalKey: 'cpName', suffixIcon: 'pi pi-search' },
        { label: '은행', field: 'bankName', type: 'text', disabled: true },
        { label: '입금자명', field: 'depo', type: 'text', disabled: true },
        { label: '입금금액', field: 'depositAmount', type: 'number', disabled: true },
        { label: '입금일자', field: 'regDt', type: 'text', disabled: true }
    ];
    columns.value = [
    { field: 'cpName', header: '거래처명', type: 'input', disabled: true },
    { field: 'unsettledAmount', header: '기존 미정산 금액', type: 'input', disabled: true },
    { field: 'depositAmount', header: '이번 입금액', type: 'input', disabled: true }, // ⬅ 계산용으로 보여주기만
    { field: 'remainingAmount', header: '남은 미정산금액', type: 'input', disabled: true }
  ];
});

// 🟧 버튼 정의
const infoFormButtons = {
    save: { show: true, label: '저장', severity: 'success' },
    reset: { show: true, label: '초기화', severity: 'secondary' },
    load: { show: true, label: '입금 내역 불러오기', severity: 'info' }
};

// 🟥 거래처 선택 모달 설정
const modalDataSets = computed(() => ({
    cpName: {
        items: companyList.value,
        columns: [
            { field: 'cpCd', header: '거래처코드' },
            { field: 'cpName', header: '거래처명' },
            { field: 'cpType', header: '거래처유형' }
        ],
        displayField: 'cpName',
        mappingFields: { 
          cpName: 'cpName',
          cpCd: 'cpCd'
        }
    }
}));

// 🟪 입출금 내역 모달 상태 및 로딩
const isCashflowDialogVisible = ref(false);

const handleLoadCashflow = async () => {
  try {
    await fetchCashflows();
    isCashflowDialogVisible.value = true;
  } catch (err) {
    toast.add({
      severity: 'error',
      summary: '불러오기 실패',
      detail: '입금 내역 불러오기 중 오류가 발생했습니다.',
      life: 3000
    });
    console.error('❌ 입금 내역 로딩 오류:', err);
  }
};

// ✅ 모달에서 항목 선택 시 반영
// 입금내역 선택 시
const handleCashflowSelected = (item) => {
  formData.value.statementCd = item.statementCd;
  formData.value.bankName = item.bankName;
  formData.value.depo = item.depo;
  formData.value.depositAmount = item.depositAmount;
  formData.value.regDt = item.regDt;
  isCashflowDialogVisible.value = false;
};

// 🔄 공통코드 및 거래처 초기 로딩
onMounted(async () => {
    await common.fetchCommonCodes('0J');
    await fetchCompanys();
    companyList.value = convertCompanyCodes(companyList.value);
});

const unpaidDetails = computed({
  get() {
    if (!formData.value.cpCd) return [];

    const company = companyList.value.find(c => c.cpCd === formData.value.cpCd);
    if (!company) return [];

    const unsettled = Number(company.unsettledAmount || 0);
    const deposit = Number(formData.value.depositAmount || 0);

    return [{
      cpCd: company.cpCd,
      cpName: company.cpName,
      unsettledAmount: unsettled,
      depositAmount: deposit,
      remainingAmount: unsettled - deposit
    }];
  },
  set() {
    // 아무 것도 안 함. 이제는 사용자 입력 안 받음.
  }
});

// 미수금 내역 버튼
const inputFormButtons = ref([]);

const handleCompanySelected = (item) => {
  formData.value.cpCd = item.cpCd;
  formData.value.cpName = item.cpName;

  if (formData.value.depositAmount) {
    const unsettled = item.unsettledAmount;
    formData.value.paidAmount = unsettled > 0
      ? Math.min(unsettled, formData.value.depositAmount)
      : 0;
  }
};

const handleSave = async () => {
  try {
    await fetchSave();

    toast.add({
      severity: 'success',
      summary: '정산 완료',
      detail: '정산이 성공적으로 완료되었습니다.',
      life: 3000
    });

    resetForm(); // 필요 시
  } catch (err) {
    toast.add({
      severity: 'error',
      summary: '정산 실패',
      detail: '정산 중 오류가 발생했습니다.',
      life: 3000
    });

    console.error('❌ 정산 오류:', err);
  }
};

</script>

<template>
    <div class="space-y-4">
        <!-- 📌 기본정보 테이블 -->
        <LeftAlignTable
            v-model:data="formData"
            title="기본정보"
            :fields="unpaidFields"
            :buttons="infoFormButtons"
            button-position="top"
            :modalDataSets="modalDataSets"
            dataKey="cpCd"
            @load="handleLoadCashflow"
            @reset="resetForm"
            @select="handleCompanySelected"
            @save="handleSave"
        />
    </div>

    <div class="space-y-4 mt-8">
        <!-- 📎 입출금 상세 입력 테이블 -->
        <InputTable :columns="columns"  v-model:data="unpaidDetails" :title="'미수금 내역'" scrollHeight="360px" height="460px" :dataKey="'cpCd'" :enableSelection="false" :buttons="inputFormButtons" :enableRowActions="false"  />
    </div>

    <!-- 💬 입금 내역 선택 모달 -->
    <Singleselect
        v-model:visible="isCashflowDialogVisible"
        :items="cashflowList"
        :itemKey="'statementCd'"
        :columns="[
            { field: 'statementCd', header: '코드' },
            { field: 'bankName', header: '은행' },
            { field: 'depo', header: '입금자' },
            { field: 'depositAmount', header: '금액' },
            { field: 'regDt', header: '입금일자' }
        ]"
        @update:modelValue="handleCashflowSelected"
    />
</template>
