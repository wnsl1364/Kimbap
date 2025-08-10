<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import { getRelOrdModal, getRelOrdSelect, getWareList, insertRelOrd } from '@/api/distribution'
import axios from 'axios'
import LeftAlignTable from '@/components/kimbap/table/LeftAlignTable.vue'
import InputTable from '@/components/kimbap/table/InputTable.vue'
import { format, parseISO } from 'date-fns'
import { storeToRefs } from 'pinia';
import { useOrderFormStore } from '@/stores/orderFormStore'
import { useOrderProductStore } from '@/stores/orderProductStore'
import { useMemberStore } from '@/stores/memberStore'
import { useRoute, useRouter } from 'vue-router';
import { reactive } from 'vue';
import { useToast } from 'primevue/usetoast';
import Toast from 'primevue/toast'

const toast = useToast();
const router = useRouter();

const infoFormButtons = reactive({});
// 로그인 정보 가져오기
const memberStore = useMemberStore()
const { user } = storeToRefs(memberStore)

// 라우터 설정
const route = useRoute()
const ordCd = route.query.ordCd
const relMasCd = route.query.relMasCd

// 스토어 인스턴스
const formStore = useOrderFormStore()
const productStore = useOrderProductStore()

// 반응형 상태
const { formData, resetForm } = storeToRefs(formStore)
const { products } = storeToRefs(productStore)

//창고 목록 상태
const warehouseList = ref([])

const showArrearsModal = ref(false)

// form 필드
const formFields1 = [
  { label: '출고지시번호', field: 'newRelMasCd', type: 'text', disabled: true },
  { label: '작성자', field: 'regi', type: 'text', disabled: true },
  { label: '출고일자', field: 'relDt', type: 'calendar', disabled: true },
  { label: '비고', field: 'note', type: 'input', disabled: false },
];
const formFields2 = [
  { label: '거래처명', field: 'cpName', type: 'input', disabled: true },
  { label: '거래처 담당자', field: 'mname', type: 'text', disabled: true },
  { label: '납품지 주소', field: 'deliAdd', type: 'text', disabled: true },
  { label: '납기요청일', field: 'deliReqDt', type: 'text', disabled: true },
]

// 제품 테이블
const columns = computed(() => [
  { field: 'prodName', header: '제품명', type: 'input', readonly: true },
  { field: 'ordQty', header: '주문수량(box)', type: 'input', inputType: 'number', align: 'right', readonly: true },
  { field: 'noRelQty', header: '주문잔여수량(box)', type: 'input', inputType: 'number', align: 'right', readonly: true },
  { field: 'relQty', header: '출고지시수량(box)', type: 'input', inputType: 'number', align: 'right', },
  {
    field: 'wcode', // 창고코드
    header: '창고',
    type: 'select',
    align: 'right',
    options: warehouseOptions.value, // ★ 변경
    optionValue: 'key',              // ★ "wcode|wareVerCd"
    optionLabel: 'label'
  },
  { field: 'relOrdStatus', header: '출고상태', type: 'input', readonly: true }
]);

const warehouseOptions = computed(() => {
  const seen = new Set()
  return (warehouseList.value || []).reduce((acc, w) => {
    const key = `${w.wcode}|${w.wareVerCd}`   // ★ 합성키
    if (!seen.has(key)) {
      seen.add(key)
      acc.push({
        key,
        label: `${w.wareName} (${w.wcode})`,
        wcode: w.wcode,
        wareVerCd: w.wareVerCd,
      })
    }
    return acc
  }, [])
})

const handleSave = async () => {
  try {
    const { newRelOrdCd, relDt, regi, note, cpCd, mname, deliAdd, deliReqDt, cpName } = formData.value;
    const ordCdResolved = formData.value?.ordCd || route.query.ordCd;
    
    if (!ordCdResolved) {
      toast.add({ 
        severity: 'warn', 
        summary: '입력 확인', 
        detail: '주문을 먼저 선택해주세요.', 
        life: 3000 
      });
      return;
    }
    
    // 필수 필드 검증 (비고 제외)
    if (!regi?.trim()) {
      toast.add({ 
        severity: 'warn', 
        summary: '입력 확인', 
        detail: '작성자를 입력해주세요.', 
        life: 3000 
      });
      return;
    }
    
    if (!cpCd?.trim()) {
      toast.add({ 
        severity: 'warn', 
        summary: '입력 확인', 
        detail: '거래처 정보가 없습니다. 주문정보를 다시 불러와주세요.', 
        life: 3000 
      });
      return;
    }
    
    if (!cpName?.trim()) {
      toast.add({ 
        severity: 'warn', 
        summary: '입력 확인', 
        detail: '거래처명이 없습니다. 주문정보를 다시 불러와주세요.', 
        life: 3000 
      });
      return;
    }
    
    if (!mname?.trim()) {
      toast.add({ 
        severity: 'warn', 
        summary: '입력 확인', 
        detail: '거래처 담당자가 없습니다. 주문정보를 다시 불러와주세요.', 
        life: 3000 
      });
      return;
    }
    
    if (!deliAdd?.trim()) {
      toast.add({ 
        severity: 'warn', 
        summary: '입력 확인', 
        detail: '납품지 주소가 없습니다. 주문정보를 다시 불러와주세요.', 
        life: 3000 
      });
      return;
    }
    
    if (!deliReqDt?.trim()) {
      toast.add({ 
        severity: 'warn', 
        summary: '입력 확인', 
        detail: '납기요청일이 없습니다. 주문정보를 다시 불러와주세요.', 
        life: 3000 
      });
      return;
    }
    
    // master VO (null 값 처리를 위해 빈 문자열로 변환)
    const master = {
      // relMasCd,
      regi: regi?.trim() || '',
      relDt,
      note: note?.trim() || '',  // null이면 빈 문자열로 처리
      cpCd: cpCd?.trim() || '',
      mname: mname?.trim() || '',
      deliAdd: deliAdd?.trim() || '',
      deliReqDt: deliReqDt?.trim() || '',
      relOrdStatus: 'm1',
      ordCd: ordCdResolved
    };

    // detailList
    const detailList = (products.value || [])
      .filter(p => Number(p.relQty) > 0)
      .map(p => {
        const [wcode, wareVerCd] = String(p.wcode || '').split('|')  // ★ 분해
        return {
          wcode,
          wareVerCd,
          ordDCd: p.ordDCd,
          relQty: Number(p.relQty || 0),
          newRelOrdCd: p.newRelOrdCd
        }
      })

    if (detailList.length === 0) {
      toast.add({ 
        severity: 'warn', 
        summary: '입력 확인', 
        detail: '출고지시수량이 입력된 제품이 없습니다.', 
        life: 3000 
      });
      return;
    }

    const payload = {
      master,
      detailList
    };

    console.log('products.value =', products.value)
    console.log('📦 등록할 출고지시 payload:', payload);

    // ✅ API 호출
    await insertRelOrd(payload);

    // ✅ 성공 처리
    toast.add({ 
      severity: 'success', 
      summary: '저장 완료', 
      detail: '출고지시가 성공적으로 저장되었습니다!', 
      life: 3000 
    });
    
    formStore.$reset();
    productStore.$reset();

    // ✅ 라우터 이동
    setTimeout(() => {
      router.push('/distribution/relOrdList');
    }, 1000);

  } catch (err) {
    console.error('❌ 출고지시 저장 실패:', err);

    // ✅ 실제 오류인 경우만 오류 메시지 표시
    const errorMessage = err.response?.data?.message || err.message || '알 수 없는 오류가 발생했습니다.';
    toast.add({ 
      severity: 'error', 
      summary: '저장 실패', 
      detail: '저장 중 오류 발생: ' + errorMessage, 
      life: 4000 
    });
  }
};

// 제품 추가 영역 버튼 설정
const purchaseFormButtons = ref({
  save: { show: false, label: '저장', severity: 'success' },
  reset: { show: false, label: '초기화', severity: 'secondary' },
  delete: { show: false, label: '삭제', severity: 'danger' },
  load: { show: false, label: '불러오기', severity: 'info' }
});

// 모달 데이터셋
const modalDataSets = ref({})

const loadOrderListForModal = async () => {
  try {
    const res = await getRelOrdModal({}) // ✅ 파라미터가 있으면 추가

    const items = res.data.map(order => ({
      ordCd: order.ordCd,
      cpName: order.cpName,
      ordDt: format(parseISO(order.ordDt), 'yyyy-MM-dd'),
      prodName: order.prodNameSummary  // ✅ 필드명 주의!
    }))
    modalDataSets.value = {
      load: {
        items,
        columns: [
          { field: 'ordCd', header: '주문코드' },
          { field: 'prodName', header: '제품명' },
          { field: 'cpName', header: '거래처명' },
          { field: 'ordDt', header: '주문일자' }
        ],
        mappingFields: {
          ordCd: 'ordCd',
          prodName: 'prodName',
          cpName: 'cpName',
          ordDt: 'ordDt'
        },
        emitEvent: 'load'
      }
    }
  } catch (err) {
    console.error('출고지시 모달 주문 목록 로딩 실패:', err)
    toast.add({ 
      severity: 'error', 
      summary: '로딩 실패', 
      detail: '주문 목록을 불러오는데 실패했습니다.', 
      life: 3000 
    });
  }
}

const handleLoadOrder = async (selectedRow) => {
  console.log('🟢 모달에서 선택된 row:', selectedRow);
  try {
    const ordCd = selectedRow.ordCd;

    // 1. 주문 상세 정보 (기존대로 불러오기)
    const orderRes = await axios.get(`/api/order/${ordCd}`);
    const order = orderRes.data.data;

    // 2. 출고지시용 제품 리스트
    const prodRes = await getRelOrdSelect(ordCd);
    const productList = (prodRes.data || []).map(p => ({
          ...p,
     relQty: 0,
     wcode: '' // 창고 셀렉트 초기화(필요 없으면 이 줄 제거)
   }));

    // 담당자명, 거래처명은 productList[0]에서 바로 꺼내기
    const mname = productList[0]?.mname || '';
    const cpName = productList[0]?.mcpName || '';
    const newRelMasCd = productList[0]?.newRelMasCd || '';

    // 3. 창고 리스트
    const wareRes = await getWareList(ordCd)
    warehouseList.value = wareRes.data || []

    // 🔥 formFields1 내 '창고' 필드의 options 갱신
    const wareField = formFields1.find(f => f.field === 'wcode');
    if (wareField) {
      wareField.options = [...warehouseList.value]; // ⭐️ 여기가 핵심
    }

    formStore.setFormData({
      ordCd: order.ordCd,
      ordDt: format(parseISO(order.ordDt), 'yyyy-MM-dd'),
      cpCd: order.cpCd,
      cpName: cpName,
      deliAdd: order.deliAdd,
      deliReqDt: format(parseISO(order.deliReqDt), 'yyyy-MM-dd'),
      exPayDt: format(parseISO(order.exPayDt), 'yyyy-MM-dd'),
      note: order.note,
      mname: mname,
      regi: user.value.empName || '',
      newRelMasCd: newRelMasCd,
      wName: '',
    });
    
    console.log('넘겨줄 데이터:', order);
    productStore.setProducts(productList);
    console.log('✅ 출고지시 제품 리스트:', productList)
    
    toast.add({ 
      severity: 'success', 
      summary: '불러오기 완료', 
      detail: '주문 정보가 성공적으로 불러와졌습니다.', 
      life: 2500 
    });
    
  } catch (err) {
    console.error('출고지시 주문 데이터 로딩 실패:', err);
    toast.add({ 
      severity: 'error', 
      summary: '불러오기 실패', 
      detail: '주문 데이터를 불러오는데 실패했습니다.', 
      life: 3000 
    });
  }
};

// 주문 불러오기
onMounted(async () => {

  if (!ordCd && !relMasCd) {
    // 주문등록 모달용 목록 로딩
    await loadOrderListForModal();
  }

  // 주문등록 모드: 쿼리로 ordCd가 넘어왔을 때 자동 주문 불러오기
  if (ordCd) {
    await handleLoadOrder({ ordCd });
  }

  // ✅ 지시서 조회 모드: relMasCd로 진입한 경우
  if (relMasCd) {
    // 출고/반려 버튼 추가
    infoFormButtons.save = {
      show: true,
      label: '출고',
      severity: 'success',
      onClick: handleSave
    };
    infoFormButtons.delete = {
      show: true,
      label: '반려',
      severity: 'danger',
      onClick: handleSave
    };

    try {
      const res = await axios.get('/api/distribution/relOrderDetail', {
        params: { relMasCd }
      });
      formStore.setFormData(res.data.master);
      productStore.setProducts(res.data.products);
      
      toast.add({ 
        severity: 'info', 
        summary: '조회 완료', 
        detail: '출고지시 상세 정보를 불러왔습니다.', 
        life: 2500 
      });
    } catch (err) {
      console.error('출고지시 상세 정보 로딩 실패:', err);
      toast.add({ 
        severity: 'error', 
        summary: '조회 실패', 
        detail: '출고지시 상세 정보를 불러오는데 실패했습니다.', 
        life: 3000 
      });
    }
  } else {
    // 신규 등록 모드
    infoFormButtons.save = {
      show: true,
      label: '저장',
      severity: 'info',
      onClick: handleSave
    };
    infoFormButtons.load = {
      show: true,
      label: '주문정보 불러오기',
      severity: 'success'
    };
  }
});

// 피니아 리셋
onUnmounted(() => {
  formStore.$reset();
  productStore.$reset();
});
</script>

<template>
  <!-- ✅ 토스트 컨테이너 추가 -->
  <Toast position="top-right" />
  
  <div class="space-y-4 mb-3">
    <LeftAlignTable v-model:data="formData" :fields="formFields1" :title="'출고 지시서'" :buttons="infoFormButtons"
      button-position="top" :modalDataSets="modalDataSets" :dataKey="'ordCd'" @save="handleSave"
      @showArrearsModal="showArrearsModal = true" @load="handleLoadOrder" @reset="handleApprove"
      @delete="handleReject" />
  </div>
  <div class="space-y-4">
    <LeftAlignTable v-model:data="formData" :fields="formFields2" :title="'출고처'" :buttons="false" button-position="top"
      :modalDataSets="modalDataSets" :dataKey="'ordCd'" @showArrearsModal="showArrearsModal = true"
      @load="handleLoadOrder" @reset="handleApprove" @delete="handleReject" />
  </div>

  <div class="space-y-4 mt-3">
    <InputTable :data="products" :columns="columns" :title="''" scrollHeight="360px" height="460px" :dataKey="'pcode'"
      :buttons="purchaseFormButtons" :enableRowActions="false" :enableSelection="false" />
  </div>
</template>