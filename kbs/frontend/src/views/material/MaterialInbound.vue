<script setup>
import { ref, onMounted, watch } from 'vue'
import { useMaterialStore } from '@/stores/materialStore'
import BasicTable from '@/components/kimbap/table/BasicTable.vue'
import InputForm from '@/components/kimbap/searchform/inputForm.vue'
// API 함수들 import
import { saveMaterialInbound, getMaterialInboundList, updateMaterialInbound } from '@/api/materials'

const materialStore = useMaterialStore()

// Store 초기화
onMounted(async () => {
    // inboundData 초기화 - VO 기반으로 수정
    materialStore.inboundData = {
        purcCd: '자동생성',           // 발주번호
        ordDt: '주문일자',            // 주문일자
        regi: '로그인된 사용자',       // 담당자
        purcStatus: '입고대기',       // 발주상태
        inboDt: '당일 일자',          // 입고일자
        wcode: ''                    // 입고창고
    };

    // inboundFields 초기화 - 기본정보 필드 (InputForm 구조에 맞게 수정)
    materialStore.inboundFields = [
        { key: 'purcCd', label: '발주번호', type: 'readonly', defaultValue: '자동생성' },
        { key: 'ordDt', label: '주문일자', type: 'readonly', defaultValue: '주문일자' },
        { key: 'regi', label: '담당자', type: 'readonly', defaultValue: '로그인된 사용자' },
        { key: 'purcStatus', label: '발주상태', type: 'readonly', defaultValue: '입고대기' },
        { key: 'inboDt', label: '입고일자', type: 'text', defaultValue: '당일 일자' },
        { 
            key: 'wcode', 
            label: '입고창고', 
            type: 'dropdown', 
            placeholder: '창고를 선택하세요',
            defaultValue: '',
            options: [
                { value: 'WARE-001', label: '냉동창고1' },
                { value: 'WARE-002', label: '원자재창고1' },
                { value: 'WARE-003', label: '냉동창고2' }
            ]
        }
    ]

    // inMaterialColumns 초기화 - 자재 테이블 컬럼
    materialStore.inMaterialColumns = [
        { field: 'mname', header: '자재명'},
        { field: 'cpName', header: '거래처명' },
        { field: 'purcQty', header: '입고요청수량' },
        { field: 'unit', header: '단위' },
        { field: 'totalQty', header: '입고수량' },
        { field: 'stoCon', header: '보관조건' },
        { field: 'exDeliDt', header: '납기예정일' },
        { field: 'deliDt', header: '납기일' },
        { field: 'note', header: '비고' }
    ]

    // ✅ 실제 DB에서 자재입고 목록 조회
    await fetchMaterialInboundData();
})

// VO 구조에 맞춘 폼 데이터
const formData = ref({
    purcCd: '',         // 발주번호
    ordDt: '',          // 주문일자
    regi: '',           // 담당자
    purcStatus: '',     // 발주상태
    inboDt: '',         // 입고일자
    wcode: ''           // 입고창고
})

// ✅ DB에서 가져온 실제 자재 데이터
const material = ref([])

// ✅ DB에서 자재입고 데이터 가져오는 함수
const fetchMaterialInboundData = async () => {
    try {
        const response = await getMaterialInboundList();
        console.log('DB에서 가져온 입고 목록:', response.data);
        
        // ✅ 입고상태가 'c3'(입고대기)인 데이터만 필터링
        const filteredData = response.data.filter(item => item.inboStatus === 'c3');
        
        // ✅ 첫 번째 데이터로 기본정보 설정
        if (filteredData && filteredData.length > 0) {
            const firstData = filteredData[0];
            formData.value = {
                purcCd: firstData.purcCd || 'PURC-' + new Date().getFullYear() + String(Date.now()).slice(-4),
                ordDt: firstData.ordDt ? formatDate(firstData.ordDt) : formatDate(new Date()),
                regi: firstData.regi || '로그인된 사용자',
                purcStatus: getInboStatusText(firstData.inboStatus) || '입고대기',
                inboDt: firstData.inboDt ? formatDate(firstData.inboDt) : formatDate(new Date()),
                wcode: ''  // 입고창고는 사용자가 선택
            };
            
            // Store에도 업데이트
            materialStore.inboundData = { ...formData.value };
        }
        
        // ✅ 입고상태가 'c3'인 데이터만 테이블에 표시
        material.value = filteredData.map((item, index) => ({
            id: index + 1,
            // MaterialsVO 필드들 매핑 (카멜 케이스 사용)
            mateInboCd: item.mateInboCd,
            mcode: item.mcode,
            mateVerCd: item.mateVerCd,
            purcDCd: item.purcDCd,  // ✅ 발주상세코드 추가
            lotNo: item.lotNo,
            supplierLotNo: item.supplierLotNo,
            wareVerCd: item.wareVerCd,
            mname: getMaterialName(item.mcode) || '임시자재1',  // 자재명
            cpName: getCompanyName(item.cpCd),          // 거래처명
            purcQty: item.purcQty || item.totalQty || 0, // 입고요청수량
            unit: item.unit || getUnitByMcode(item.mcode), // 단위
            totalQty: item.totalQty || 0,               // 입고수량
            stoCon: getStorageCondition(item.mcode),    // 보관조건
            exDeliDt: item.exDeliDt ? formatDateForTable(item.exDeliDt) : '', // 납기예정일
            deliDt: item.deliDt ? formatDateForTable(item.deliDt) : '',       // 납기일
            note: item.note || '',                      // 비고
            // 추가 정보들
            wcode: item.wcode,
            inboDt: item.inboDt ? formatDateForTable(item.inboDt) : '',
            inboStatus: item.inboStatus,
            purcStatus: item.purcStatus,
            cpCd: item.cpCd
        }));
        
        console.log('변환된 테이블 데이터 (입고상태 c3만):', material.value);
        
    } catch (error) {
        console.error('입고 목록 조회 실패:', error);
        
        // ✅ API 실패 시 기본정보도 기본값으로 설정
        formData.value = {
            purcCd: 'PURC-001',
            ordDt: formatDate(new Date()),
            regi: '김김밥',
            purcStatus: '입고대기',
            inboDt: formatDate(new Date()),
            wcode: ''
        };
        
        // ✅ API 실패 시 임시 데이터 1개만 생성 (발주상태 c3)
        material.value = [
            {   
                id: 1,
                mateInboCd: 'MATI-202507-0001',
                mcode: 'MAT-1001',
                mateVerCd: 'V1',
                mname: '임시자재1',
                cpName: '황금쌀농협',
                purcQty: 100,
                unit: 'kg',
                totalQty: 100,
                stoCon: '상온',
                exDeliDt: '2025-07-21',
                deliDt: '2025-07-20',
                note: '임시자재1 입고대기',
                lotNo: 'LOT-100-20250720-1',
                wcode: 'WARE-002',
                inboDt: '2025-07-20',
                inboStatus: 'c3',
                purcStatus: 'c3'  // 입고대기 상태
            }
        ];
        
        alert('서버에서 데이터를 가져올 수 없어 샘플 데이터를 표시합니다.');
    }
}

// ✅ 날짜 포맷팅 함수 (기본정보용 - YYYY-MM-DD 형식)
const formatDate = (dateInput) => {
    if (!dateInput) return '';
    
    let date;
    if (typeof dateInput === 'string') {
        // ISO 형식 문자열인 경우
        date = new Date(dateInput);
    } else if (dateInput instanceof Date) {
        date = dateInput;
    } else {
        return '';
    }
    
    // 유효한 날짜인지 확인
    if (isNaN(date.getTime())) {
        return '';
    }
    
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    
    return `${year}-${month}-${day}`;
}

// ✅ 테이블용 날짜 포맷팅 함수 (YYYY-MM-DD 형식)
const formatDateForTable = (dateInput) => {
    if (!dateInput) return '';
    
    let date;
    if (typeof dateInput === 'string') {
        // ISO 형식 문자열인 경우
        date = new Date(dateInput);
    } else if (dateInput instanceof Date) {
        date = dateInput;
    } else {
        return '';
    }
    
    // 유효한 날짜인지 확인
    if (isNaN(date.getTime())) {
        return '';
    }
    
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    
    return `${year}-${month}-${day}`;
}
const getInboStatusText = (status) => {
    switch(status) {
        case 'c1': return '발주요청';
        case 'c2': return '발주승인';
        case 'c3': return '입고대기';
        case 'c4': return '부분입고';
        case 'c5': return '입고완료';
        case 'c6': return '발주거절';
        case 'c7': return '반품';
        default: return '입고대기';
    }
}

// ✅ 입고상태 코드를 한글로 변환하는 함수
const getMaterialName = (mcode) => {
    const materialMap = {
        'MAT-1001': '프리미엄쌀',
        'MAT-1002': '신선야채믹스',
        'MAT-1003': '참치마요소스',
        'MAT-1004': '프리미엄소세지',
        'MAT-1005': '김밥김',
        'MAT-2001': '냉동김밥포장지',
        'MAT-2002': '김밥박스'
    };
    return materialMap[mcode] || '임시자재1';
}

// ✅ 자재코드로 단위를 가져오는 함수
const getUnitByMcode = (mcode) => {
    const unitMap = {
        'MAT-1001': 'kg',
        'MAT-1002': 'kg',
        'MAT-1003': 'kg',
        'MAT-1004': 'ea',
        'MAT-1005': 'ea',
        'MAT-2001': 'ea',
        'MAT-2002': 'ea'
    };
    return unitMap[mcode] || 'ea';
}
// ✅ 회사명으로 회사코드 가져오는 함수 (역매핑)
const getCompanyCodeByName = (cpName) => {
    const companyCodeMap = {
        '신선야채농장': 'CP-001',
        '맛있는참치회사': 'CP-002',
        '프리미엄소세지': 'CP-003',
        '황금쌀농협': 'CP-004',
        '포장의달인': 'CP-005'
    };
    return companyCodeMap[cpName] || 'CP-001';
}
// ✅ 회사코드로 회사명 가져오는 함수 (실제로는 DB 조회)
const getCompanyName = (cpCd) => {
    const companyMap = {
        'CP-001': '신선야채농장',
        'CP-002': '맛있는참치회사',
        'CP-003': '프리미엄소세지',
        'CP-004': '황금쌀농협',
        'CP-005': '포장의달인'
    };
    return companyMap[cpCd] || '공급업체';
}

// ✅ 자재코드로 보관조건 유추하는 함수 (실제로는 material 테이블에서 조회)
const getStorageCondition = (mcode) => {
    if (mcode && mcode.includes('1001')) return '상온';  // 쌀
    if (mcode && mcode.includes('1002')) return '냉장';  // 야채
    if (mcode && mcode.includes('1003')) return '냉장';  // 참치마요
    if (mcode && mcode.includes('1004')) return '냉장';  // 소세지
    if (mcode && mcode.includes('1005')) return '상온';  // 김
    if (mcode && mcode.includes('2001')) return '상온';  // 포장지
    return '상온';
}

// ✅ 폼 초기화 핸들러 - 입고창고와 선택된 자재만 초기화
const handleFormReset = () => {
    // 입고창고만 초기화 (다른 필드는 유지)
    formData.value = {
        ...formData.value,  // 기존 값들 유지
        wcode: ''           // 입고창고만 초기화
    };
    
    // Store에도 업데이트
    materialStore.inboundData = { ...formData.value };
    
    // 선택된 자재들만 초기화
    selectedMaterials.value = [];
    
    console.log('입고창고 선택과 자재 선택이 초기화되었습니다.');
}
const selectedMaterials = ref([])

// 선택된 자재들이 변경될 때마다 Store에 자동으로 저장
watch(selectedMaterials, (newSelection) => {
    console.log('선택된 자재 변경:', newSelection);
    materialStore.setSelectedInboundMaterials([...newSelection]);
}, { deep: true })

// 선택된 자재들
const handleInboundComplete = async () => {
  try {
    // 유효성 검증
    if (!formData.value.wcode) {
      alert('입고창고를 선택해주세요.');
      return;
    }

    // 선택된 자재가 있는지 확인
    if (!selectedMaterials.value || selectedMaterials.value.length === 0) {
      alert('입고할 자재를 선택해주세요.');
      return;
    }

    // ✅ 기존 데이터 수정 방식으로 변경 - 납기일 추가 및 상태 변경
    const materialUpdateDataList = selectedMaterials.value.map((material) => ({
      // 기존 데이터는 그대로 유지
      mateInboCd: material.mateInboCd,
      mcode: material.mcode,
      mateVerCd: material.mateVerCd,
      wcode: formData.value.wcode,  // 사용자가 선택한 입고창고로 변경
      wareVerCd: material.wareVerCd || 'V1',
      purcDCd: material.purcDCd || 'PURC-D-DEFAULT-' + material.mateInboCd,  // null이면 기본값 생성
      lotNo: material.lotNo,
      supplierLotNo: material.supplierLotNo,
      inboDt: material.inboDt,  // 기존 입고일자 유지
      inboStatus: 'c5',  // 입고완료로 상태 변경
      totalQty: material.totalQty,
      mname: material.mname,
      note: material.note || `${material.mname} 입고처리 완료`,
      cpCd: material.cpCd,
      deliDt: new Date()  // 현재 시간으로 납기일 추가
    }));

    console.log('자재입고 수정용 데이터:', materialUpdateDataList);
    
    // ✅ 디버깅: purcDCd 값 확인
    console.log('선택된 자재의 purcDCd 값:', selectedMaterials.value.map(m => ({
      mateInboCd: m.mateInboCd,
      purcDCd: m.purcDCd,
      purcDCdType: typeof m.purcDCd
    })));

    // ✅ 실제 API 호출 (UPDATE 방식)
    for (const updateData of materialUpdateDataList) {
      const response = await updateMaterialInbound(updateData);
      console.log('UPDATE API 응답:', response.data);
    }
    
    // Store에 데이터 저장
    materialStore.inboundData = { 
      ...formData.value,
      purcStatus: '입고완료',
      materials: [...selectedMaterials.value]
    };
    
    // 처리된 자재들을 히스토리에 추가
    materialStore.addProcessedInboundMaterials([...selectedMaterials.value]);
    
    // 상태 업데이트
    formData.value.purcStatus = '입고완료';
    
    alert(`입고 처리가 완료되었습니다. (처리된 자재: ${selectedMaterials.value.length}개)`);
    
    // 선택 초기화 
    selectedMaterials.value = [];
    
    // 데이터 새로고침
    await fetchMaterialInboundData();
    
  } catch (error) {
    console.error('입고 처리 중 오류 발생:', error);
    
    // 에러 메시지 처리
    let errorMessage = '입고 처리 중 오류가 발생했습니다.';
    
    if (error.response) {
      errorMessage = `입고 처리 실패: ${error.response.data?.message || '서버 오류'}`;
      console.log('서버 에러 상세:', error.response);
    } else if (error.request) {
      errorMessage = '서버와 통신할 수 없습니다. 네트워크를 확인해주세요.';
    }
    
    alert(errorMessage);
  }
}

</script>

<template>
    <div class="flex justify-between mb-2">
        <!-- 기본정보는 InputForm의 title로 이동 -->
    </div>
    <div class="space-y-4 mb-2" >
        <!-- 발주번호, 주문일자, 담당자, 발주상태, 입고일자, 입고창고 -->
        <InputForm 
            :columns="materialStore.inboundFields"
            :data="formData" 
            title="기본정보"
            :buttons="{ 
                save: { show: true, label: '입고처리', severity: 'success' }, 
                reset: { show: false, label: '초기화', severity: 'secondary' },
                delete: { show: false, label: '삭제', severity: 'danger' },
                load: { show: false, label: '불러오기', severity: 'info' }
            }"
            @update:data="(newData) => { formData = newData; materialStore.inboundData = { ...newData }; }"
            @submit="handleInboundComplete"
        />
    </div>
    <div>
        <h2>자재 목록</h2>
        <!-- 자재명, 거래처명, 입고요청수량, 단위, 입고수량, 보관조건, 납기예정일, 납기일, 비고 -->
        <BasicTable 
            :data="material" 
            :columns="materialStore.inMaterialColumns" 
            v-model:selection="selectedMaterials"
            selectionMode="multiple"
            :dataKey="'id'"
        />
    </div>
</template>