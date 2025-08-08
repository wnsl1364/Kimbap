<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import { getRelOrdModal, getRelOrdSelect, getWareList, insertRelOrd, getRelOrdList, getMasterInfo } from '@/api/distribution'
import axios from 'axios'
import LeftAlignTable from '@/components/kimbap/table/LeftAlignTable.vue'
import InputTable from '@/components/kimbap/table/InputTable.vue'
import { format, parseISO } from 'date-fns'
import { storeToRefs } from 'pinia';
import { useOrderFormStore } from '@/stores/orderFormStore'
import { useOrderProductStore } from '@/stores/orderProductStore'
import { useRoute } from 'vue-router';
import { useMemberStore } from '@/stores/memberStore';

const memberStore = useMemberStore();
const { user } = storeToRefs(memberStore);

const today = format(new Date(), 'yyyy-MM-dd');

// 라우터 설정
const route = useRoute()
const ordCd = route.query.ordCd

// 스토어 인스턴스
const formStore = useOrderFormStore()
const productStore = useOrderProductStore()

// 반응형 상태
const { formData } = storeToRefs(formStore)

//창고 목록 상태
const warehouseList = ref([])

const showArrearsModal = ref(false)

// form 필드
const formFields1 = [
  { label: '출고지시번호', field: 'newRelMasCd', type: 'text', disabled: true },
  { label: '출고일자', field: 'relDt', type: 'text', disabled: true , defaultValue: today }
];

// 제품 테이블
const columns1 = computed(() => [
  { field: 'prodName', header: '제품명', type: 'input', readonly: true },
  { field: 'ordQty', header: '주문수량(box)', type: 'input', inputType: 'number', align: 'right', readonly: true},
  { field: 'relQty', header: '출고지시수량(box)', type: 'input', inputType: 'number', align: 'right'},
  { field: 'relOrdStatus', header: 'LOT번호', type: 'input', align: 'left', readonly: true }
]);

const columns2 = computed(() => [
  { field: 'prodName', header: '제품명', type: 'input', readonly: true },
  { field: 'ordQty', header: 'LOT번호', type: 'input', inputType: 'number', align: 'right', readonly: true},
  { field: 'relQty', header: '출고수량(box)', type: 'input', inputType: 'number', align: 'right'},
]);

const handleSave = async () => {
  try {
    const { newRelOrdCd, relDt, regi, note, cpCd, mname, deliAdd, deliReqDt } = formData.value;

    const master = {
      regi,
      relDt,
      note,
      cpCd,
      mname,
      deliAdd,
      deliReqDt,
      relOrdStatus: 'm1'
    };

    // detailList
    const detailList = (products.value || [])
      .filter(p => p.relQty > 0)
      .map(p => ({
        wcode: p.wcode,
        wareVerCd: p.wareVerCd,
        ordDCd: p.ordDCd,
        relQty: p.relQty,
        newRelOrdCd: p.newRelOrdCd
      }))
      
    if (detailList.length === 0) {
      alert('출고지시수량이 입력된 제품이 없습니다.');
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
    alert('출고지시 저장 완료!');
    formStore.$reset();
    productStore.$reset();
    
    
  } catch (err) {
    console.error('❌ 출고지시 저장 실패:', err);
    
    // ✅ 실제 오류인 경우만 오류 메시지 표시
    const errorMessage = err.response?.data?.message || err.message || '알 수 없는 오류가 발생했습니다.';
    alert('저장 중 오류 발생: ' + errorMessage);
  }
};


// 버튼 설정
const infoFormButtons = ref({
  save: { show: true, label: '출고', severity: 'info', onClick: handleSave },
  load: { show: true, label: '출고지시서 불러오기', severity: 'success' },
});

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
    const res = await getRelOrdList({}) // ✅ 파라미터가 있으면 추가

    const items = res.data.map(relOrdList => ({
      relMasCd: relOrdList.relMasCd,
      cpName: relOrdList.cpName,
      prodName: relOrdList.prodName,
      relDt:relOrdList.relDt
    }))
    modalDataSets.value = {
      load: {
        items,
        columns: [
          { field: 'relMasCd', header: '출고지시번호' },
          { field: 'cpName', header: '거래처명' },
          { field: 'prodName', header: '제품명' },
          { field: 'relDt', header: '지시일자' }
        ],
        mappingFields: {
          relMasCd: 'relMasCd',
          cpName: 'cpName',
          prodName: 'prodName',
          relDt: 'relDt'
        },
        emitEvent: 'load'
      }
    }
  } catch (err) {
    console.error('출고지시 모달 주문 목록 로딩 실패:', err)
  }
}

const handleLoadOrder = async (selectedRow) => {
  try {
    const relMasCd = selectedRow.relMasCd;

    // 1. 마스터 + 제품목록을 한 번에 GET!
    const res = await axios.get('/api/distribution/relOrdDetail', { params: { relMasCd } });
    const master = res.data.master;
    const products = res.data.products;

    // 2. formData에 마스터 정보 세팅
    formStore.setFormData({
      ...master,
      regi: user.value.empName || '',
      // 필요하면 추가 필드 변환/매핑
    });

    // 3. 제품리스트 store에 세팅
    productStore.setProducts(products);

    // 4. (필요하다면) 창고 목록 조회
    if (products && products.length > 0) {
      const ordCd = products[0].ordCd; // 제품 목록에서 주문코드 꺼내기
      const wareRes = await getWareList(ordCd);
      warehouseList.value = wareRes.data || [];
      const wareField = formFields1.find(f => f.field === 'wcode');
      if (wareField) {
        wareField.options = [...warehouseList.value];
      }
    }

    console.log('넘겨줄 데이터:', master);
    console.log('✅ 출고지시 제품 리스트:', products);
  } catch (err) {
    console.error('출고지시 주문 데이터 로딩 실패:', err);
  }
};



// 주문 불러오기
onMounted(async () => {
  if (!ordCd) {
    await loadOrderListForModal();
  }

  // 자동 주문 불러오기
  if (ordCd) {
    await handleLoadOrder({ ordCd })
  }
})
// 피니아 리셋
onUnmounted(() => {
  formStore.$reset();
});
</script>

<template>
  <div class="space-y-4 mb-3">
    <LeftAlignTable v-model:data="formData" :fields="formFields1" :title="'출고 지시서'" :buttons="infoFormButtons"
      button-position="top" :modalDataSets="modalDataSets" :dataKey="'relCd'" @save="handleSave"
      @showArrearsModal="showArrearsModal = true" @load="handleLoadOrder" @reset="handleApprove"
      @delete="handleReject" />
  </div>
  <div class="space-y-4 mt-3">
    <InputTable :data="products" :columns="columns1" :title="''" scrollHeight="250px" height="305px" :dataKey="'pcode'"
      :buttons="purchaseFormButtons" :enableRowActions="false" :enableSelection="false" />
  </div>
    <div class="space-y-4 mt-3">
    <InputTable :data="products" :columns="columns2" :title="''" scrollHeight="250px" height="305px" :dataKey="'pcode'"
      :buttons="purchaseFormButtons" :enableRowActions="false" :enableSelection="false" />
  </div>
</template>
