<script setup>
import { ref, onMounted, watch } from 'vue'
import { useMaterialStore } from '@/stores/materialStore'
import LeftAlignTable from '@/components/kimbap/table/LeftAlignTable.vue'
import BasicTable from '@/components/kimbap/table/BasicTable.vue'
import Singleselect from '@/components/kimbap/modal/singleselect.vue'

const materialStore = useMaterialStore()

// Store 초기화
onMounted(() => {
    // inboundData 초기화
    materialStore.inboundData = {
        purcCd: '자동생성',
        status: '입고대기',
        ordDt: '주문일자', 
        inboDt: '당일 일자',
        mname: '로그인된 사용자',
        wcode: ''
    };

    // inboundFields 초기화
    materialStore.inboundFields = [
        { label: '입고번호', field: 'purcCd', type: 'text', readonly: true },
        { label: '상태', field: 'status', type: 'text', readonly: true },
        { label: '주문일자', field: 'ordDt', type: 'date', readonly: true },
        { label: '입고일자', field: 'inboDt', type: 'date', readonly: false },
        { label: '등록자', field: 'mname', type: 'text', readonly: true },
        { label: '입고창고', field: 'wcode', type: 'input', readonly: true, suffixIcon: 'pi pi-search', suffixEvent: 'openWarehouseModal' }
    ]

    // inMaterialColumns 초기화
    materialStore.inMaterialColumns = [
        { field: 'mateName', header: '자재명'},
        { field: 'cpName', header: '거래처명' },
        { field: 'purcQty', header: '입고요청수량' },
        { field: 'unit', header: '단위' }, 
        { field: 'total_qty', header: '입고수량' },
        { field: 'ex_deli_dt', header: '납기예정일'},
        { field: 'deli_dt', header: '납기일'},
        { field: 'note', header: '비고' }
    ]

    // warehouseColumns 초기화
    materialStore.warehouseColumns = [
        { field: 'name', header: '공장이름' },
        { field: 'category', header: '창고명' },
        { field: 'type', header: '창고유형' },
        { field: 'quantity', header: '창고담당자' }
    ]
})

const formData = ref({
    purcCd: '자동생성',
    status: '입고대기',
    ordDt: '주문일자', 
    inboDt: '당일 일자',
    mname: '로그인된 사용자',
    wcode: ''
})

const material = ref([
    {   id: 1,
        mateName: '김밥용 김',
        cpName: '광동식자재',
        purcQty: 1000,
        unit: '장',
        total_qty: 980,
        ex_deli_dt: '2025-07-24',
        deli_dt: '2025-07-22',
        note: '부분 납품됨'
    },
    {   
        id: 2,
        mateName: '햄',
        cpName: '한성푸드',
        purcQty: 500,
        unit: '개',
        total_qty: 500,
        ex_deli_dt: '2025-07-24',
        deli_dt: '2025-07-22',
        note: ''
    },
    {   
        id: 3,
        mateName: '단무지',
        cpName: '맛있는식자재',
        purcQty: 300,
        unit: 'kg',
        total_qty: 300,
        ex_deli_dt: '2025-07-24',
        deli_dt: '2025-07-22',
        note: '정상 입고'
    }
])

// 선택된 자재들
const selectedMaterials = ref([])

// 선택된 자재들이 변경될 때마다 Store에 자동으로 저장
watch(selectedMaterials, (newSelection) => {
    console.log('선택된 자재 변경:', newSelection);
    materialStore.setSelectedInboundMaterials([...newSelection]);
}, { deep: true })

const warehouseList = ref([
    { id: 1, name: 'A', category: 'A', type: '냉동', quantity: '김냉동' },
    { id: 2, name: 'A', category: 'B', type: '냉장', quantity: '이냉장' },
    { id: 3, name: 'A', category: 'C', type: '실온', quantity: '박실온' },
    { id: 4, name: 'B', category: 'A', type: '냉동', quantity: '최냉동' },
    { id: 5, name: 'B', category: 'B', type: '냉장', quantity: '곽냉장' },
    { id: 6, name: 'B', category: 'C', type: '실온', quantity: '이실온' },
])

const isWarehouseModalOpen = ref(false)
const selectedWarehouse = ref(null)

const openWarehouseModal = () => {
  isWarehouseModalOpen.value = true
}

const onConfirmWarehouse = (value) => {
  if (value){
    selectedWarehouse.value = value;
    formData.value.wcode = `${value.name}-${value.category}-${value.type}`;
    // Store 업데이트 (안전한 접근)
    if (materialStore.inboundData) {
      materialStore.inboundData.wcode = `${value.name}-${value.category}-${value.type}`;
    }
  } else {
    selectedWarehouse.value = null;
    formData.value.wcode = ''; 
    if (materialStore.inboundData) {
      materialStore.inboundData.wcode = '';
    }
  }
  isWarehouseModalOpen.value = false;
}

// 입고 완료 처리 함수
const handleInboundComplete = async () => {
  try {
    // 유효성 검증
    if (!formData.value.wcode) {
      alert('입고창고를 선택해주세요.');
      return;
    }

    // 선택된 자재가 있는지 확인 (주석 처리)
    // if (!selectedMaterials.value || selectedMaterials.value.length === 0) {
    //   alert('입고할 자재를 선택해주세요.');
    //   return;
    // }

    // 입고 데이터 구성 (선택된 자재만 포함)
    const inboundData = {
      purcCd: formData.value.purcCd,
      status: '입고완료',
      ordDt: formData.value.ordDt,
      inboDt: formData.value.inboDt,
      mname: formData.value.mname,
      wcode: formData.value.wcode,
      materials: [...selectedMaterials.value]  // 선택된 자재만 저장 (깊은 복사)
    };

    console.log('입고 처리 데이터:', inboundData);
    console.log('선택된 자재 수:', selectedMaterials.value.length);
    console.log('Store에 저장된 선택 자재:', materialStore.selectedInboundMaterials);

    // Store에 데이터 저장
    materialStore.inboundData = { ...inboundData };
    
    // 처리된 자재들을 히스토리에 추가
    materialStore.addProcessedInboundMaterials([...selectedMaterials.value]);
    
    // 상태 업데이트
    formData.value.status = '입고완료';
    
    // TODO: 실제 API 호출 부분
    // const response = await api.saveInbound(inboundData);
    
    alert(`입고 처리가 완료되었습니다. (처리된 자재: ${selectedMaterials.value.length}개)`);
    
    // 선택 초기화 (선택사항)
    // selectedMaterials.value = [];
    
  } catch (error) {
    console.error('입고 처리 중 오류 발생:', error);
    alert('입고 처리 중 오류가 발생했습니다.');
  }
}

</script>

<template>
    <div class="flex justify-between mb-2">
        <h2>기본정보</h2>
        <button 
            class="p-button p-component h-10" 
            type="button" 
            aria-label="Primary" 
            @click="handleInboundComplete"
            data-pc-name="button" 
            data-p-disabled="false" 
            pc68="" 
            data-pc-section="root">
            <span class="p-button-label" data-pc-section="label">입고처리</span>
        </button>
    </div>
    <div class="space-y-4 mb-2" >
        <LeftAlignTable :data="formData" :fields="materialStore.inboundFields" @openWarehouseModal="openWarehouseModal"/>
    </div>
    <div>
        <h2>자재 목록</h2>
        <!-- 선택된 자재 개수 표시 -->
        <div class="mb-2 text-sm text-gray-600">
            선택된 자재: {{ selectedMaterials.length }}개
        </div>
        <BasicTable 
            :data="material" 
            :columns="materialStore.inMaterialColumns" 
            v-model:selection="selectedMaterials"
            selectionMode="multiple"
            :dataKey="'id'"
        />
    </div>

    <Singleselect
        v-model:visible="isWarehouseModalOpen"
        v-model:modelValue="selectedWarehouse"
        :items="warehouseList"
        itemKey="id"
        :columns="materialStore.warehouseColumns"
        @update:modelValue="onConfirmWarehouse"
    />
</template>