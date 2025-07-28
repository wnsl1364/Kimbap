<script setup>
import { ref, onMounted, watch } from 'vue'
import { useMaterialStore } from '@/stores/materialStore'
import BasicTable from '@/components/kimbap/table/BasicTable.vue'
import InputForm from '@/components/kimbap/searchform/inputForm.vue'
// API 함수들 import
import { getMaterialInboundList, updateMaterialInbound } from '@/api/materials'

const materialStore = useMaterialStore()

// Store 초기화
onMounted(async () => {
    // DB에서 공장 목록 불러오기
    await loadFactoryList();
    // 실제 DB에서 자재입고 목록 조회
    await fetchMaterialInboundData();
})

// 공장 목록 상태
const factoryList = ref([])

// DB에서 공장 목록 가져오는 함수
const loadFactoryList = async () => {
    try {
        const response = await fetch('/api/materials/factories');
        
        // ✅ 응답 상태 체크
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const factories = await response.json();
        
        console.log('DB에서 가져온 공장 목록:', factories);
        
        // ✅ 데이터 유효성 체크
        if (!Array.isArray(factories)) {
            throw new Error('공장 목록 데이터가 배열이 아닙니다.');
        }
        
        // 공장 목록을 드롭다운 옵션 형태로 변환
        factoryList.value = factories.map(factory => ({
            value: factory.fcode,
            label: `${factory.facName} (${factory.fcode})`,
            facVerCd: factory.facVerCd, 
            address: factory.address,
            tel: factory.tel,
            manager: factory.mname
        }));
        
        // ✅ inboundFields 초기화 (방어 코드 추가)
        if (factoryList.value && factoryList.value.length > 0) {
            materialStore.inboundFields = [
                { key: 'purcCd', label: '발주번호', type: 'readonly' },
                { key: 'ordDt', label: '주문일자', type: 'readonly' },
                { key: 'regi', label: '담당자', type: 'readonly' },
                { key: 'purcStatus', label: '발주상태', type: 'readonly' },
                // ✅ 납기일자로 변경 - 이미 존재하는 납기일 표시
                { key: 'deliDt', label: '납기일자', type: 'readonly' },
                { 
                    key: 'fcode', 
                    label: '입고공장', 
                    type: 'dropdown', 
                    placeholder: '⚠️ 입고공장을 선택해주세요',
                    options: factoryList.value,
                    required: true
                }
            ];
        } else {
            console.warn('공장 목록이 비어있습니다.');
            // 공장 목록이 없을 때 기본 설정
            materialStore.inboundFields = [
                { key: 'purcCd', label: '발주번호', type: 'readonly' },
                { key: 'ordDt', label: '주문일자', type: 'readonly' },
                { key: 'regi', label: '담당자', type: 'readonly' },
                { key: 'purcStatus', label: '발주상태', type: 'readonly' },
                { key: 'deliDt', label: '납기일자', type: 'readonly' },
                { 
                    key: 'fcode', 
                    label: '입고공장', 
                    type: 'text', 
                    placeholder: '공장 목록을 불러올 수 없습니다',
                    disabled: true
                }
            ];
        }
        
    } catch (error) {
        console.error('공장 목록 로드 실패:', error);
        alert(`공장 목록을 불러오는데 실패했습니다: ${error.message}`);
        
        // ✅ 오류 시 기본 필드 설정
        materialStore.inboundFields = [
            { key: 'purcCd', label: '발주번호', type: 'readonly' },
            { key: 'ordDt', label: '주문일자', type: 'readonly' },
            { key: 'regi', label: '담당자', type: 'readonly' },
            { key: 'purcStatus', label: '발주상태', type: 'readonly' },
                { key: 'deliDt', label: '납기일자', type: 'readonly' },
            { 
                key: 'fcode', 
                label: '입고공장', 
                type: 'text', 
                placeholder: '공장 목록 로드 실패',
                disabled: true
            }
        ];
    }
}

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

// 폼 데이터
const formData = ref({
    purcCd: '',
    ordDt: '',
    regi: '',
    purcStatus: '',
    deliDt: '',  // ✅ 납기일자로 변경
    fcode: ''
})

// DB에서 가져온 실제 자재 데이터
const material = ref([])

// DB에서 자재입고 데이터 가져오는 함수
const fetchMaterialInboundData = async () => {
    const response = await getMaterialInboundList();
    console.log('DB에서 가져온 입고 목록:', response.data);
    
    // 입고상태가 'c3'(입고대기)인 데이터만 필터링
    const filteredData = response.data.filter(item => item.inboStatus === 'c3');
    
    // 첫 번째 데이터로 기본정보 설정 (입고공장은 비우기!)
    if (filteredData && filteredData.length > 0) {
        const firstData = filteredData[0];
        formData.value = {
            purcCd: firstData.purcCd || '',
            ordDt: firstData.ordDt ? formatDateForTable(firstData.ordDt) : '',
            regi: firstData.regiName || firstData.regi || '담당자 정보 없음',  // ✅ 안전하게 처리
            purcStatus: getInboStatusText(firstData.inboStatus),
            deliDt: firstData.deliDt ? formatDateForTable(firstData.deliDt) : '',  // ✅ 납기일자 표시
            fcode: ''  // ✅ 입고공장은 항상 빈 상태로 시작! (DB 값 무시)
        };
        
        // Store에도 업데이트
        materialStore.inboundData = { ...formData.value };
        
        console.log('폼 데이터 설정 완료 (납기일자 표시, 입고공장 초기화):', formData.value);
    }
    
    // 입고상태가 'c3'인 데이터만 테이블에 표시
    material.value = filteredData.map((item, index) => ({
        id: index + 1,
        mateInboCd: item.mateInboCd,
        mcode: item.mcode,
        mateVerCd: item.mateVerCd,
        purcDCd: item.purcDCd,
        lotNo: item.lotNo,
        supplierLotNo: item.supplierLotNo,
        wcode: item.wcode,
        wareVerCd: item.wareVerCd,
        fcode: item.fcode,
        facVerCd: item.facVerCd,
        mname: item.mateName || item.mname,
        cpName: item.cpName,
        purcQty: item.purcQty || item.totalQty,
        unit: getUnitText(item.unit),
        totalQty: item.totalQty,
        stoCon: getStorageConditionText(item.stoCon),
        exDeliDt: item.exDeliDt ? formatDateForTable(item.exDeliDt) : '',
        deliDt: item.deliDt ? formatDateForTable(item.deliDt) : '', // ✅ 납기일은 이미 존재하는 값 표시
        note: item.note,
        // ✅ inboDt는 테이블에서 제외하거나 빈 값으로 표시 (아직 입고 안됨)
        inboDt: '',  // 입고 전이므로 빈 값
        inboStatus: item.inboStatus,
        purcStatus: item.purcStatus,
        cpCd: item.cpCd,
        // ✅ 발주 정보도 포함
        purcCd: item.purcCd,
        ordDt: item.ordDt,
        regi: item.regi  // 원래대로 복원
    }));
    
    console.log('변환된 테이블 데이터 (입고상태 c3만):', material.value);
}

const formatDateForTable = (dateInput) => {
    if (!dateInput) return '';
    
    let date;
    if (typeof dateInput === 'string') {
        date = new Date(dateInput);
    } else if (dateInput instanceof Date) {
        date = dateInput;
    } else {
        return '';
    }
    
    if (isNaN(date.getTime())) {
        return '';
    }
    
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    
    return `${year}-${month}-${day}`;
}

// 현재 날짜를 YYYY-MM-DD 형식으로 반환하는 함수
const getCurrentDate = () => {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

// 보관조건 코드를 한글로 변환하는 함수
const getStorageConditionText = (stoCon) => {
    switch(stoCon) {
        case 'o1': return '상온';
        case 'o2': return '냉장';
        case 'o3': return '냉동';
        default: return stoCon;
    }
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
        default: return status;
    }
}

const getUnitText = (unit) => {
    switch(unit) {
        case 'g1': return 'g';
        case 'g2': return 'kg';
        case 'g3': return 'ml';
        case 'g4': return 'L';
        case 'g5': return 'ea';
        case 'g6': return 'box';
        case 'g7': return 'mm';
        default: return unit;
    }
}

const selectedMaterials = ref([])

// 선택된 자재들이 변경될 때마다 Store에 자동으로 저장
watch(selectedMaterials, (newSelection) => {
    console.log('선택된 자재 변경:', newSelection);
    materialStore.setSelectedInboundMaterials([...newSelection]);
}, { deep: true })

// 입고처리 함수 (상태 변경)
const handleInboundComplete = async () => {
    // ✅ 입고공장 선택 여부 체크 강화
    if (!formData.value.fcode || formData.value.fcode === '') {  
        alert('입고공장을 반드시 선택해주세요!');
        return;
    }

    // 선택된 자재가 있는지 확인
    if (!selectedMaterials.value || selectedMaterials.value.length === 0) {
        alert('입고할 자재를 선택해주세요.');
        return;
    }

    // facVerCd가 설정되어 있는지 확인하고, 없으면 다시 찾기
    if (!formData.value.facVerCd) {
        const selectedFactory = factoryList.value.find(f => f.value === formData.value.fcode);
        if (selectedFactory && selectedFactory.facVerCd) {
            formData.value.facVerCd = selectedFactory.facVerCd;
        } else {
            alert('선택된 공장의 버전 정보를 찾을 수 없습니다.');
            return;
        }
    }

    // ✅ 입고처리 시점에서 입고일자를 현재 날짜로 설정 (기본정보에는 표시되지 않음)
    const currentDate = getCurrentDate();

    console.log('✅ 입고공장 선택 확인:', formData.value.fcode);
    console.log('✅ 입고처리 날짜 설정:', currentDate);
    console.log('최종 formData:', formData.value);

    try {
        // 기존 데이터 수정 방식으로 변경 - 입고공장 업데이트 및 상태 변경
        const materialUpdateDataList = selectedMaterials.value.map((material) => ({
            mateInboCd: material.mateInboCd,
            mcode: material.mcode,
            mateVerCd: material.mateVerCd,
            wcode: material.wcode,
            wareVerCd: material.wareVerCd,
            fcode: formData.value.fcode,  // ✅ 사용자가 선택한 입고공장으로 업데이트
            facVerCd: formData.value.facVerCd,  // 올바른 facVerCd 사용
            purcDCd: material.purcDCd,
            lotNo: material.lotNo,
            supplierLotNo: material.supplierLotNo,
            inboDt: currentDate,  // ✅ 입고일자는 현재 날짜로 설정
            inboStatus: 'c5',  // 입고완료로 상태 변경
            totalQty: material.totalQty,
            mname: material.mname,
            note: material.note,
            cpCd: material.cpCd,
            deliDt: material.deliDt  // ✅ 기존 납기일은 그대로 유지 (변경하지 않음)
        }));

        console.log('UPDATE 전송 데이터 (입고일자 현재날짜 적용):', materialUpdateDataList[0]);
        
        // 실제 API 호출 (UPDATE 방식)
        for (const updateData of materialUpdateDataList) {
            console.log(`${updateData.mateInboCd} 업데이트: fcode=${updateData.fcode}, inboDt=${updateData.inboDt}`);
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
        
        alert(`입고 처리가 완료되었습니다. (처리된 자재: ${selectedMaterials.value.length}개, 입고일자: ${currentDate})`);
        
        // 선택 초기화 및 입고공장도 다시 비우기
        selectedMaterials.value = [];
        formData.value.fcode = '';  // ✅ 입고공장 다시 비우기
        formData.value.facVerCd = '';
        // ✅ 납기일자는 그대로 유지 (기본정보에서 계속 표시)
        
        // 데이터 새로고침
        await fetchMaterialInboundData();
        
    } catch (error) {
        console.error('입고 처리 중 오류 발생:', error);
        
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
    <div class="space-y-4 mb-2">
        <!-- 발주번호, 주문일자, 담당자, 발주상태, 납기일자, 입고공장 -->
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
            @update:data="(newData) => {
                formData = newData;

                // ✅ 선택한 fcode에 대응하는 facVerCd 찾아서 자동 설정 (방어 코드 추가)
                if (factoryList.value && Array.isArray(factoryList.value) && newData.fcode) {
                    const selectedFactory = factoryList.value.find(f => f.value === newData.fcode);
                    if (selectedFactory && selectedFactory.facVerCd) {
                        formData.facVerCd = selectedFactory.facVerCd;
                        console.log('공장 선택:', selectedFactory.label, '버전코드:', selectedFactory.facVerCd);
                    }
                }

                materialStore.inboundData = { ...formData };
            }"
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