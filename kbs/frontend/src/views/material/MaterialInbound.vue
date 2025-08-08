<script setup>
import { ref, onMounted, watch } from 'vue'
import { useMaterialStore } from '@/stores/materialStore'
import { useToast } from 'primevue/usetoast'
import BasicTable from '@/components/kimbap/table/BasicTable.vue'
import InputForm from '@/components/kimbap/searchform/inputForm.vue'
import { getMaterialInboundList, getPurchaseOrderDetailsForInbound, insertMaterialInbound } from '@/api/materials'
import { useRoute } from 'vue-router'

const materialStore = useMaterialStore()
const toast = useToast()
const route = useRoute()

// URL 파라미터에서 발주번호 가져오기
const purcCd = ref(route.query.purcCd || '')

onMounted(async () => {
    await loadFactoryList();
    
    if (purcCd.value) {
        // 🔥 발주번호가 있을 때만 purc_ord_d 기반 조회
        await fetchPurchaseOrderData();
    } else {
        // 🔥 발주번호가 없으면 안내 메시지
        console.log('⚠️ 발주번호가 없습니다. 발주 목록에서 입고대기 상태를 선택해주세요.');
        toast.add({
            severity: 'warn',
            summary: '발주번호 필요',
            detail: '발주번호가 필요합니다. 발주 목록에서 입고대기 상태의 발주를 선택해주세요.',
            life: 5000
        });
        material.value = [];
    }
})

const factoryList = ref([])

const loadFactoryList = async () => {
    try {
        const response = await fetch('/api/materials/factories');
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const factories = await response.json();
        
        if (!Array.isArray(factories)) {
            throw new Error('공장 목록 데이터가 배열이 아닙니다.');
        }
        
        factoryList.value = factories.map(factory => ({
            value: factory.fcode,
            label: `${factory.facName} (${factory.fcode})`,
            facVerCd: factory.facVerCd, 
            address: factory.address,
            tel: factory.tel,
            manager: factory.mname
        }));
        
        // 공장 목록이 있을 때만 필드 설정
        if (factoryList.value.length > 0) {
            materialStore.inboundFields = [
                { key: 'purcCd', label: '발주번호', type: 'readonly' },
                { key: 'ordDt', label: '주문일자', type: 'readonly' },
                { key: 'regi', label: '담당자', type: 'readonly' },
                { key: 'purcStatus', label: '발주상태', type: 'readonly' },
                { key: 'deliDt', label: '납기일자', type: 'readonly' },
                { 
                    key: 'fcode', 
                    label: '입고공장', 
                    type: 'dropdown', 
                    placeholder: '입고공장을 선택해주세요',
                    options: factoryList.value,
                    required: true
                }
            ];
        }
        
    } catch (error) {
        console.error('공장 목록 로드 실패:', error);
        toast.add({
            severity: 'error',
            summary: '공장 목록 로드 실패',
            detail: `공장 목록을 불러오는데 실패했습니다: ${error.message}`,
            life: 5000
        });
    }
}

materialStore.inMaterialColumns = [
    { field: 'purcDCd', header: '발주상세번호'},
    { field: 'mname', header: '자재명'},
    { field: 'cpName', header: '거래처명' },
    { field: 'purcQty', header: '입고요청수량', align: 'right' },
    { field: 'unit', header: '단위' },
    { field: 'totalQty', header: '입고수량', align: 'right' },
    { field: 'stoCon', header: '보관조건' },
    { field: 'exDeliDt', header: '납기예정일' },
    { field: 'deliDt', header: '납기일' },
    { field: 'note', header: '비고' }
]

const formData = ref({
    purcCd: '',
    ordDt: '',
    regi: '',
    purcStatus: '',
    deliDt: '',
    fcode: ''
})

const material = ref([])

// 🔥 특정 발주번호의 입고대기 자재 조회 (purc_ord_d 기반)
const fetchPurchaseOrderData = async () => {
    try {
        console.log('🔍 발주번호로 입고대기 자재 조회 시작:', purcCd.value);
        
        const response = await getPurchaseOrderDetailsForInbound(purcCd.value);
        
        if (response.data && response.data.length > 0) {
            const orderData = response.data[0];
            
            // 상단 폼에 발주 기본 정보 설정
            formData.value = {
                purcCd: orderData.purcCd || '',
                ordDt: orderData.ordDt ? formatDateForTable(orderData.ordDt) : '',
                regi: orderData.regiName || orderData.regi || '담당자 정보 없음',
                purcStatus: getInboStatusText('c3'), // 입고대기 상태 표시
                deliDt: orderData.deliDt ? formatDateForTable(orderData.deliDt) : '',
                fcode: ''
            };
            
            materialStore.inboundData = { ...formData.value };
            
            // 하단 목록에 해당 발주번호의 자재들 표시
            material.value = response.data.map((item, index) => ({
                id: index + 1,
                purcDCd: item.purcDCd,
                mcode: item.mcode,
                mateVerCd: item.mateVerCd,
                mname: item.mateName || item.mname,
                cpName: item.cpName,
                purcQty: item.purcQty,
                unit: getUnitText(item.unit),
                totalQty: item.purcQty, // 입고요청수량을 기본값으로
                stoCon: getStorageConditionText(item.stoCon),
                exDeliDt: item.exDeliDt ? formatDateForTable(item.exDeliDt) : '',
                deliDt: item.deliDt ? formatDateForTable(item.deliDt) : '',
                note: item.note || '',
                purcDStatus: item.purcDStatus,
                cpCd: item.cpCd,
                purcCd: item.purcCd,
                ordDt: item.ordDt,
                regi: item.regi
            }));
            
            console.log('✅ 발주번호 기반 자재 조회 성공:', material.value.length, '건');
        } else {
            console.log('⚠️ 해당 발주번호의 입고대기 자재가 없습니다.');
            toast.add({
                severity: 'warn',
                summary: '자료 없음',
                detail: '해당 발주번호의 입고대기 자재가 없습니다.',
                life: 4000
            });
            material.value = [];
        }
    } catch (error) {
        console.error('❌ 발주 자료 조회 실패:', error);
        
        let errorMessage = '발주 정보를 불러오는데 실패했습니다.';
        if (error.response?.status === 500) {
            errorMessage = '백엔드 API 오류입니다. 개발팀에 문의해주세요.';
        } else if (error.response?.status === 404) {
            errorMessage = '해당 발주번호의 정보를 찾을 수 없습니다.';
        }
        
        toast.add({
            severity: 'error',
            summary: '조회 실패',
            detail: errorMessage,
            life: 5000
        });
        material.value = [];
    }
}

// 🔥 mate_inbo 테이블 기반 조회는 제거됨 - 오직 purc_ord_d 기반만 사용

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

const getCurrentDate = () => {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

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

watch(selectedMaterials, (newSelection) => {
    materialStore.setSelectedInboundMaterials([...newSelection]);
}, { deep: true })

const handleInboundComplete = async () => {
    if (!purcCd.value) {
        toast.add({
            severity: 'warn',
            summary: '발주번호 필요',
            detail: '발주번호가 없습니다. 발주 목록에서 입고대기 상태를 선택해주세요.',
            life: 4000
        });
        return;
    }

    if (!formData.value.fcode || formData.value.fcode === '') {  
        toast.add({
            severity: 'warn',
            summary: '입고공장 선택 필요',
            detail: '입고공장을 반드시 선택해주세요!',
            life: 4000
        });
        return;
    }

    if (!selectedMaterials.value || selectedMaterials.value.length === 0) {
        toast.add({
            severity: 'warn',
            summary: '자재 선택 필요',
            detail: '입고할 자재를 선택해주세요.',
            life: 4000
        });
        return;
    }

    if (!formData.value.facVerCd) {
        // 안전한 체크 추가
        const selectedFactory = factoryList.value?.find(f => f.value === formData.value.fcode);
        if (selectedFactory?.facVerCd) {
            formData.value.facVerCd = selectedFactory.facVerCd;
        } else {
            toast.add({
                severity: 'error',
                summary: '공장 정보 오류',
                detail: '선택된 공장의 버전 정보를 찾을 수 없습니다.',
                life: 4000
            });
            return;
        }
    }

    const currentDate = getCurrentDate();

    try {
        // 🔥 purc_ord_d 기반 입고 처리: mate_inbo 테이블에 신규 등록
        const materialInboundDataList = selectedMaterials.value.map((material) => ({
            mcode: material.mcode,
            mateVerCd: material.mateVerCd,
            purcDCd: material.purcDCd,
            fcode: formData.value.fcode,
            facVerCd: formData.value.facVerCd,
            totalQty: material.totalQty,
            inboDt: currentDate,
            inboStatus: 'c5', // 입고완료
            note: material.note || `${material.mname} 입고완료`,
            cpCd: material.cpCd,
            deliDt: currentDate
        }));
        
        // mate_inbo 테이블에 새로 등록
        for (const inboundData of materialInboundDataList) {
            await insertMaterialInbound(inboundData);
        }
        
        toast.add({
            severity: 'success',
            summary: '입고 처리 완료',
            detail: `입고 처리가 완료되었습니다. (처리된 자재: ${selectedMaterials.value.length}개, 입고일자: ${currentDate})`,
            life: 5000
        });
        
        materialStore.inboundData = { 
            ...formData.value,
            purcStatus: '입고완료',
            materials: [...selectedMaterials.value]
        };
        
        materialStore.addProcessedInboundMaterials([...selectedMaterials.value]);
        
        formData.value.purcStatus = '입고완료';
        
        selectedMaterials.value = [];
        formData.value.fcode = '';
        formData.value.facVerCd = '';
        
        // 데이터 다시 조회
        await fetchPurchaseOrderData();
        
    } catch (error) {
        let errorMessage = '입고 처리 중 오류가 발생했습니다.';
        
        if (error.response) {
            errorMessage = `입고 처리 실패: ${error.response.data?.message || '서버 오류'}`;
        } else if (error.request) {
            errorMessage = '서버와 통신할 수 없습니다. 네트워크를 확인해주세요.';
        }
        
        toast.add({
            severity: 'error',
            summary: '입고 처리 실패',
            detail: errorMessage,
            life: 5000
        });
    }
}

</script>

<template>
    <div class="space-y-4 mb-2">
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

                // 안전한 체크 추가
                if (factoryList.value && Array.isArray(factoryList.value) && newData.fcode) {
                    const selectedFactory = factoryList.value.find(f => f.value === newData.fcode);
                    if (selectedFactory?.facVerCd) {
                        formData.facVerCd = selectedFactory.facVerCd;
                    }
                }

                materialStore.inboundData = { ...formData };
            }"
            @submit="handleInboundComplete"
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