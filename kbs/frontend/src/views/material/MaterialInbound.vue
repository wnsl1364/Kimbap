<script setup>
import { ref, onMounted, watch } from 'vue'
import { useMaterialStore } from '@/stores/materialStore'
import BasicTable from '@/components/kimbap/table/BasicTable.vue'
import LeftTableDropdown from '@/components/kimbap/table/LeftTableDropdown.vue'
// API 함수들 import
import { saveMaterialInbound, getMaterialInboundList } from '@/api/materials'

const materialStore = useMaterialStore()

// Store 초기화
onMounted(async () => {
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
        { 
            label: '입고창고', 
            field: 'wcode', 
            type: 'dropdown', 
            placeholder: '창고를 선택하세요',
            options: [
                { value: 'A', label: 'A공장' },
                { value: 'B', label: 'B공장' },
                { value: 'C', label: 'C공장' }
            ]
        }
    ]

    // inMaterialColumns 초기화
    materialStore.inMaterialColumns = [
        { field: 'mname', header: '자재명'},
        { field: 'mateInboCd', header: '입고코드' },
        { field: 'totalQty', header: '입고수량' },
        { field: 'inboStatus', header: '입고상태' }, 
        { field: 'lotNo', header: '로트번호' },
        { field: 'wcode', header: '입고창고' },
        { field: 'inboDt', header: '입고일자' },
        { field: 'deliDt', header: '납기일' },
        { field: 'note', header: '비고' }
    ]

    // ✅ 실제 DB에서 자재입고 목록 조회
    await fetchMaterialInboundData();
})

const formData = ref({
    purcCd: '',  // DB에서 불러올 예정
    status: '',  // DB에서 불러올 예정
    ordDt: '', 
    inboDt: '',
    mname: '',  // DB에서 불러올 예정
    wcode: ''   // 사용자가 입력
})

// ✅ DB에서 가져온 실제 자재 데이터
const material = ref([])

// ✅ DB에서 자재입고 데이터 가져오는 함수
const fetchMaterialInboundData = async () => {
    try {
        const response = await getMaterialInboundList();
        console.log('DB에서 가져온 입고 목록:', response.data);
        
        // ✅ 첫 번째 데이터로 기본정보 설정 (입고창고는 제외)
        if (response.data && response.data.length > 0) {
            const firstData = response.data[0];
            formData.value = {
                purcCd: firstData.mateInboCd || '자동생성',
                status: getStatusText(firstData.inboStatus) || '입고대기',
                ordDt: firstData.inboDt ? new Date(firstData.inboDt).toLocaleDateString('ko-KR') : new Date().toLocaleDateString('ko-KR'),
                inboDt: firstData.inboDt ? new Date(firstData.inboDt).toLocaleDateString('ko-KR') : new Date().toLocaleDateString('ko-KR'),
                mname: '로그인된 사용자',  // 실제로는 로그인 사용자 정보
                wcode: ''  // 입고창고는 사용자가 선택
            };
            
            // Store에도 업데이트
            materialStore.inboundData = { ...formData.value };
        }
        
        // DB 데이터를 테이블에 맞는 형태로 변환
        material.value = response.data.map((item, index) => ({
            id: index + 1,
            mateInboCd: item.mateInboCd,
            mname: item.mname,
            totalQty: item.totalQty,
            inboStatus: item.inboStatus,
            lotNo: item.lotNo,
            wcode: item.wcode,
            inboDt: item.inboDt ? new Date(item.inboDt).toISOString().split('T')[0] : '',
            deliDt: item.deliDt ? new Date(item.deliDt).toISOString().split('T')[0] : '',
            note: item.note || '',
            // 기존 하드코딩 필드들도 포함 (호환성)
            mateName: item.mname,
            cpName: '공급업체',
            purcQty: item.totalQty,
            unit: '개',
            stoCon: '실온',
            exDeliDt: item.deliDt ? new Date(item.deliDt).toISOString().split('T')[0] : ''
        }));
        
        console.log('변환된 테이블 데이터:', material.value);
        
    } catch (error) {
        console.error('입고 목록 조회 실패:', error);
        
        // ✅ API 실패 시 기본정보도 기본값으로 설정
        formData.value = {
            purcCd: 'INB2025001',
            status: '입고대기',
            ordDt: '2025-07-01',
            inboDt: new Date().toLocaleDateString('ko-KR'),
            mname: '로그인된 사용자',
            wcode: ''
        };
        
        // ✅ API 실패 시 기존 하드코딩 데이터 사용 (폴백)
        material.value = [
            {   id: 1,
                mateInboCd: 'INB2025001',
                mname: '김밥용 김',
                mateName: '김밥용 김',
                cpName: '광동식자재',
                purcQty: 1000,
                unit: '장',
                totalQty: 980,
                inboStatus: 'WAITING',
                stoCon: '실온',
                exDeliDt: '2025-07-24',
                deliDt: '2025-07-22',
                inboDt: '2025-07-22',
                note: '부분 납품됨',
                lotNo: 'LOT20250722001',
                wcode: 'A'
            },
            {   
                id: 2,
                mateInboCd: 'INB2025002',
                mname: '햄',
                mateName: '햄',
                cpName: '한성푸드',
                purcQty: 500,
                unit: '개',
                totalQty: 500,
                inboStatus: 'COMPLETE',
                stoCon: '냉동', 
                exDeliDt: '2025-07-24',
                deliDt: '2025-07-22',
                inboDt: '2025-07-22',
                note: '',
                lotNo: 'LOT20250722002',
                wcode: 'A'
            },
            {   
                id: 3,
                mateInboCd: 'INB2025003',
                mname: '단무지',
                mateName: '단무지',
                cpName: '맛있는식자재',
                purcQty: 300,
                unit: 'kg',
                totalQty: 300,
                inboStatus: 'COMPLETE',
                stoCon: '냉장', 
                exDeliDt: '2025-07-24',
                deliDt: '2025-07-22',
                inboDt: '2025-07-22',
                note: '정상 입고',
                lotNo: 'LOT20250722003',
                wcode: 'B'
            }
        ];
        
        alert('서버에서 데이터를 가져올 수 없어 샘플 데이터를 표시합니다.');
    }
}

// ✅ 입고상태 코드를 한글로 변환하는 함수
const getStatusText = (status) => {
    switch(status) {
        case 'WAITING': return '입고대기';
        case 'PROCESS': return '진행중';
        case 'COMPLETE': return '입고완료';
        default: return '입고대기';
    }
}

// 선택된 자재들
const selectedMaterials = ref([])

// 선택된 자재들이 변경될 때마다 Store에 자동으로 저장
watch(selectedMaterials, (newSelection) => {
    console.log('선택된 자재 변경:', newSelection);
    materialStore.setSelectedInboundMaterials([...newSelection]);
}, { deep: true })

// ✅ 입고 완료 처리 함수 - 실제 DB 저장
const handleInboundComplete = async () => {
  try {
    // 유효성 검증
    if (!formData.value.wcode) {
      alert('입고창고를 입력해주세요.');
      return;
    }

    // 선택된 자재가 있는지 확인
    if (!selectedMaterials.value || selectedMaterials.value.length === 0) {
      alert('입고할 자재를 선택해주세요.');
      return;
    }

    // ✅ 실제 DB 저장용 데이터 구성
    const mateInboData = {
      mateInboCd: 'INB' + Date.now(),  // 입고코드 자동생성
      mcode: 'MAT001',  // 자재코드 (고정값으로 테스트)
      mateVerCd: 'V001',
      wcode: formData.value.wcode || 'A',  // 기본값 설정           
      wareVerCd: 'WV001',
      purcDCd: null,  // 발주상세코드 (없으면 null)
      lotNo: 'LOT' + new Date().toISOString().slice(0,10).replace(/-/g,'') + '001',
      supplierLotNo: 'SP-LOT-' + Date.now(),
      inboDt: new Date().toISOString(), // ISO 문자열로 전송
      inboStatus: 'COMPLETE',  // Oracle DB 8자 제한에 맞춤                    
      totalQty: selectedMaterials.value.reduce((sum, item) => sum + (item.totalQty || 0), 0) || 1, // 최소 1개
      mname: selectedMaterials.value[0]?.mname || selectedMaterials.value[0]?.mateName || '테스트자재',
      note: `${selectedMaterials.value.length}개 자재 일괄 입고`, 
      cpCd: 'CP001',  // 회사코드
      deliDt: new Date().toISOString() // ISO 문자열로 전송
    };

    console.log('DB 저장용 데이터:', mateInboData);

    // ✅ 실제 API 호출
    const response = await saveMaterialInbound(mateInboData);
    
    console.log('API 응답:', response.data);
    
    // Store에 데이터 저장
    materialStore.inboundData = { 
      ...formData.value,
      status: '입고완료',
      materials: [...selectedMaterials.value]
    };
    
    // 처리된 자재들을 히스토리에 추가
    materialStore.addProcessedInboundMaterials([...selectedMaterials.value]);
    
    // 상태 업데이트
    formData.value.status = '입고완료';
    
    alert(`입고 처리가 완료되었습니다. (처리된 자재: ${selectedMaterials.value.length}개)`);
    
    // 선택 초기화 
    selectedMaterials.value = [];
    
  } catch (error) {
    console.error('입고 처리 중 오류 발생:', error);
    
    // 에러 메시지 처리
    let errorMessage = '입고 처리 중 오류가 발생했습니다.';
    
    if (error.response) {
      // 서버에서 에러 응답을 받은 경우
      errorMessage = `입고 처리 실패: ${error.response.data || '서버 오류'}`;
      console.log('서버 에러 상세:', error.response);
    } else if (error.request) {
      // 요청이 전송되었지만 응답을 받지 못한 경우
      errorMessage = '서버와 통신할 수 없습니다. 네트워크를 확인해주세요.';
    }
    
    alert(errorMessage);
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
        <LeftTableDropdown 
            :data="formData" 
            :fields="materialStore.inboundFields" 
            @update:data="(newData) => { formData = newData; materialStore.inboundData = { ...newData }; }"
        />
    </div>
    <div>
        <h2>자재 목록</h2>
        <BasicTable 
            :data="material" 
            :columns="materialStore.inMaterialColumns" 
            v-model:selection="selectedMaterials"
            selectionMode="multiple"
            :dataKey="'id'"
        />
    </div>
</template>