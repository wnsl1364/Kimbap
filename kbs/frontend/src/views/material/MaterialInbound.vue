<script setup>
import { ref, onMounted, watch } from 'vue'
import { useMaterialStore } from '@/stores/materialStore'
import BasicTable from '@/components/kimbap/table/BasicTable.vue'
import InputForm from '@/components/kimbap/searchform/inputForm.vue'
import { getMaterialInboundList, updateMaterialInbound } from '@/api/materials'

const materialStore = useMaterialStore()

onMounted(async () => {
    await loadFactoryList();
    await fetchMaterialInboundData();
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
        
        if (factoryList.value && factoryList.value.length > 0) {
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
                    placeholder: '⚠️ 입고공장을 선택해주세요',
                    options: factoryList.value,
                    required: true
                }
            ];
        } else {
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
        alert(`공장 목록을 불러오는데 실패했습니다: ${error.message}`);
        
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

materialStore.inMaterialColumns = [
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

const fetchMaterialInboundData = async () => {
    const response = await getMaterialInboundList();
    
    const filteredData = response.data.filter(item => item.inboStatus === 'c3');
    
    if (filteredData && filteredData.length > 0) {
        const firstData = filteredData[0];
        formData.value = {
            purcCd: firstData.purcCd || '',
            ordDt: firstData.ordDt ? formatDateForTable(firstData.ordDt) : '',
            regi: firstData.regiName || firstData.regi || '담당자 정보 없음',
            purcStatus: getInboStatusText(firstData.inboStatus),
            deliDt: firstData.deliDt ? formatDateForTable(firstData.deliDt) : '',
            fcode: ''
        };
        
        materialStore.inboundData = { ...formData.value };
    }
    
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
        deliDt: item.deliDt ? formatDateForTable(item.deliDt) : '',
        note: item.note,
        inboDt: '',
        inboStatus: item.inboStatus,
        purcStatus: item.purcStatus,
        cpCd: item.cpCd,
        purcCd: item.purcCd,
        ordDt: item.ordDt,
        regi: item.regi
    }));
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
    if (!formData.value.fcode || formData.value.fcode === '') {  
        alert('입고공장을 반드시 선택해주세요!');
        return;
    }

    if (!selectedMaterials.value || selectedMaterials.value.length === 0) {
        alert('입고할 자재를 선택해주세요.');
        return;
    }

    if (!formData.value.facVerCd) {
        const selectedFactory = factoryList.value.find(f => f.value === formData.value.fcode);
        if (selectedFactory && selectedFactory.facVerCd) {
            formData.value.facVerCd = selectedFactory.facVerCd;
        } else {
            alert('선택된 공장의 버전 정보를 찾을 수 없습니다.');
            return;
        }
    }

    const currentDate = getCurrentDate();

    try {
        const materialUpdateDataList = selectedMaterials.value.map((material) => ({
            mateInboCd: material.mateInboCd,
            mcode: material.mcode,
            mateVerCd: material.mateVerCd,
            wcode: material.wcode,
            wareVerCd: material.wareVerCd,
            fcode: formData.value.fcode,
            facVerCd: formData.value.facVerCd,
            purcDCd: material.purcDCd,
            lotNo: material.lotNo,
            supplierLotNo: material.supplierLotNo,
            inboDt: currentDate,
            inboStatus: 'c5',
            totalQty: material.totalQty,
            mname: material.mname,
            note: material.note,
            cpCd: material.cpCd,
            deliDt: material.deliDt
        }));
        
        for (const updateData of materialUpdateDataList) {
            const response = await updateMaterialInbound(updateData);
        }
        
        materialStore.inboundData = { 
            ...formData.value,
            purcStatus: '입고완료',
            materials: [...selectedMaterials.value]
        };
        
        materialStore.addProcessedInboundMaterials([...selectedMaterials.value]);
        
        formData.value.purcStatus = '입고완료';
        
        alert(`입고 처리가 완료되었습니다. (처리된 자재: ${selectedMaterials.value.length}개, 입고일자: ${currentDate})`);
        
        selectedMaterials.value = [];
        formData.value.fcode = '';
        formData.value.facVerCd = '';
        
        await fetchMaterialInboundData();
        
    } catch (error) {
        let errorMessage = '입고 처리 중 오류가 발생했습니다.';
        
        if (error.response) {
            errorMessage = `입고 처리 실패: ${error.response.data?.message || '서버 오류'}`;
        } else if (error.request) {
            errorMessage = '서버와 통신할 수 없습니다. 네트워크를 확인해주세요.';
        }
        
        alert(errorMessage);
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

                if (factoryList.value && Array.isArray(factoryList.value) && newData.fcode) {
                    const selectedFactory = factoryList.value.find(f => f.value === newData.fcode);
                    if (selectedFactory && selectedFactory.facVerCd) {
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