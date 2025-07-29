<script setup>
import { ref, onMounted, watch } from 'vue'
import { useMaterialStore } from '@/stores/materialStore'
import InputTable from '@/components/kimbap/table/InputTable.vue'
import SearchThreeCols from '@/components/kimbap/searchform/SearchThreeCols.vue'
// API 함수들 import
import { getMaterialInboundList } from '@/api/materials'

const materialStore = useMaterialStore()

// 검색 조건 데이터
const searchData = ref({
    mateInboCd: '',
    mcode: '',
    mname: '',
    fcode: '',
    stoCon: '',
    dateRange: null
})

// 공장 목록 상태
const factoryList = ref([])

// 적재 대기 자재 목록 상태
const storageWaitingList = ref([])

// Store 초기화
onMounted(async () => {
    // DB에서 공장 목록 불러오기
    await loadFactoryList();
    // 실제 DB에서 적재 대기 목록 조회
    await fetchStorageWaitingData();
})

// DB에서 공장 목록 가져오는 함수
const loadFactoryList = async () => {
    try {
        const response = await fetch('/api/materials/factories');
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const factories = await response.json();
        
        console.log('DB에서 가져온 공장 목록:', factories);
        
        if (!Array.isArray(factories)) {
            throw new Error('공장 목록 데이터가 배열이 아닙니다.');
        }
        
        // 공장 목록을 드롭다운 옵션 형태로 변환
        factoryList.value = [
            { label: '전체', value: '' },
            ...factories.map(factory => ({
                value: factory.fcode,
                label: `${factory.facName} (${factory.fcode})`,
                facVerCd: factory.facVerCd
            }))
        ];
        
        // 검색 조건 설정
        setupSearchColumns();
        
    } catch (error) {
        console.error('공장 목록 로드 실패:', error);
        alert(`공장 목록을 불러오는데 실패했습니다: ${error.message}`);
        
        // 오류 시 기본 검색 조건 설정
        setupSearchColumns();
    }
}

// 검색 조건 설정
const setupSearchColumns = () => {
    materialStore.searchColumns = [
        {
            key: 'mateInboCd',
            label: '입고번호',
            type: 'text',
            placeholder: '입고번호를 입력하세요'
        },
        {
            key: 'mcode',
            label: '자재코드',
            type: 'text',
            placeholder: '자재코드를 입력하세요'
        },
        {
            key: 'mname',
            label: '자재명',
            type: 'text',
            placeholder: '자재명을 입력하세요',
            rowBreak: true 
        },
        {
            key: 'fcode',
            label: '공장',
            type: 'dropdown',
            options: factoryList.value.length > 0 ? factoryList.value : [
                { label: '공장 목록을 불러오는 중...', value: '' }
            ],
            placeholder: '공장을 선택하세요',
        },
        {
            key: 'stoCon',
            label: '보관조건',
            type: 'dropdown',
            options: [
                { label: '전체', value: '' },
                { label: '상온', value: 'o1' },
                { label: '냉장', value: 'o2' },
                { label: '냉동', value: 'o3' }
            ],
            placeholder: '보관조건을 선택하세요'
        },
        {
            key: 'dateRange',
            label: '입고일자',
            type: 'dateRange',
            placeholder: '입고일자를 선택하세요'
        }
    ];
}

// 테이블 컬럼 설정
const tableColumns = ref([
    { field: 'mateInboCd', header: '입고번호', type: 'readonly' },
    { field: 'inboDt', header: '입고일자', type: 'readonly' },
    { field: 'mcode', header: '자재코드', type: 'readonly' },
    { field: 'mname', header: '자재명', type: 'readonly' },
    { field: 'facName', header: '공장', type: 'readonly' },
    { field: 'stoCon', header: '보관조건', type: 'readonly' },
    { field: 'lotNo', header: 'LOT번호', type: 'readonly' },
    { field: 'totalQty', header: '적재대기수량', type: 'readonly' },
    { field: 'unit', header: '단위', type: 'readonly' },
    { field: 'note', header: '비고', type: 'readonly' }
])

// 테이블 버튼 설정
const tableButtons = ref({
    save: { show: false, label: '저장', severity: 'success' },
    reset: { show: false, label: '초기화', severity: 'secondary' },
    delete: { show: false, label: '삭제', severity: 'danger' },
    load: { show: false, label: '불러오기', severity: 'info' }
})

// 폼 버튼들 숨기기
materialStore.purchaseFormButtons = {
    save: { show: false, label: '저장', severity: 'success' },
    reset: { show: false, label: '초기화', severity: 'secondary' },
    delete: { show: false, label: '삭제', severity: 'danger' },
    load: { show: false, label: '불러오기', severity: 'info' }
};

// DB에서 적재 대기 자재 데이터 가져오는 함수
const fetchStorageWaitingData = async () => {
    try {
        const response = await getMaterialInboundList();
        console.log('DB에서 가져온 입고 목록:', response.data);
        
        // 입고상태가 'c5'(입고완료)인 데이터만 필터링
        const completedInboundData = response.data.filter(item => item.inboStatus === 'c5');
        
        console.log('입고완료된 데이터 (적재대기):', completedInboundData);
        
        // 테이블 표시용으로 데이터 변환
        storageWaitingList.value = completedInboundData.map((item, index) => ({
            id: index + 1,
            mateInboCd: item.mateInboCd,
            inboDt: item.inboDt ? formatDateForTable(item.inboDt) : '',
            mcode: item.mcode,
            mname: item.mateName || item.mname,
            facName: item.facName || getFactoryName(item.fcode),
            stoCon: getStorageConditionText(item.stoCon),
            lotNo: item.lotNo,
            totalQty: item.totalQty,
            unit: getUnitText(item.unit),
            note: item.note || '',
            // 원본 데이터 보관 (검색용)
            _original: {
                fcode: item.fcode,
                stoConCode: item.stoCon,
                inboDtRaw: item.inboDt
            }
        }));
        
        // 필터링 적용
        applyFilters();
        
    } catch (error) {
        console.error('적재 대기 목록 조회 실패:', error);
        alert('적재 대기 목록을 불러오는데 실패했습니다.');
    }
}

// 공장명 가져오기
const getFactoryName = (fcode) => {
    if (!fcode) return '';
    const factory = factoryList.value.find(f => f.value === fcode);
    return factory ? factory.label : fcode;
}

// 날짜 포맷팅
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

// 보관조건 코드를 한글로 변환하는 함수
const getStorageConditionText = (stoCon) => {
    switch(stoCon) {
        case 'o1': return '상온';
        case 'o2': return '냉장';
        case 'o3': return '냉동';
        default: return stoCon || '';
    }
}

// 단위 코드를 한글로 변환하는 함수
const getUnitText = (unit) => {
    switch(unit) {
        case 'g1': return 'g';
        case 'g2': return 'kg';
        case 'g3': return 'ml';
        case 'g4': return 'L';
        case 'g5': return 'ea';
        case 'g6': return 'box';
        case 'g7': return 'mm';
        default: return unit || '';
    }
}

// 필터링된 데이터
const filteredStorageList = ref([])

// 필터링 적용 함수
const applyFilters = () => {
    let filtered = [...storageWaitingList.value];
    
    // 입고번호 필터
    if (searchData.value.mateInboCd && searchData.value.mateInboCd.trim() !== '') {
        filtered = filtered.filter(item => 
            item.mateInboCd && item.mateInboCd.toLowerCase().includes(searchData.value.mateInboCd.toLowerCase())
        );
    }
    
    // 자재코드 필터
    if (searchData.value.mcode && searchData.value.mcode.trim() !== '') {
        filtered = filtered.filter(item => 
            item.mcode && item.mcode.toLowerCase().includes(searchData.value.mcode.toLowerCase())
        );
    }
    
    // 자재명 필터
    if (searchData.value.mname && searchData.value.mname.trim() !== '') {
        filtered = filtered.filter(item => 
            item.mname && item.mname.toLowerCase().includes(searchData.value.mname.toLowerCase())
        );
    }
    
    // 공장 필터
    if (searchData.value.fcode && searchData.value.fcode !== '') {
        filtered = filtered.filter(item => 
            item._original.fcode === searchData.value.fcode
        );
    }
    
    // 보관조건 필터
    if (searchData.value.stoCon && searchData.value.stoCon !== '') {
        filtered = filtered.filter(item => 
            item._original.stoConCode === searchData.value.stoCon
        );
    }
    
    // 입고일자 범위 필터
    if (searchData.value.dateRange && searchData.value.dateRange !== null) {
        // dateRange가 { start, end } 객체인 경우
        if (searchData.value.dateRange.start || searchData.value.dateRange.end) {
            filtered = filtered.filter(item => {
                if (!item._original.inboDtRaw) return false;
                const itemDate = new Date(item._original.inboDtRaw);
                
                // 시작일 체크
                if (searchData.value.dateRange.start) {
                    const start = new Date(searchData.value.dateRange.start);
                    start.setHours(0, 0, 0, 0);
                    if (itemDate < start) return false;
                }
                
                // 종료일 체크
                if (searchData.value.dateRange.end) {
                    const end = new Date(searchData.value.dateRange.end);
                    end.setHours(23, 59, 59, 999);
                    if (itemDate > end) return false;
                }
                
                return true;
            });
        }
    }
    
    filteredStorageList.value = filtered;
    
    console.log('필터링 결과:', {
        원본개수: storageWaitingList.value.length,
        필터링후: filteredStorageList.value.length,
        검색조건: searchData.value
    });
}


// 검색 폼에서 검색 실행 시
const handleSearch = () => {
    console.log('검색 실행:', searchData.value);
    applyFilters(); // 검색 버튼 클릭 시에만 필터링 적용
}

// 검색 조건 초기화
const handleReset = () => {
    searchData.value = {
        mateInboCd: '',
        mcode: '',
        mname: '',
        fcode: '',
        stoCon: '',
        dateRange: null
    };
    applyFilters();
}

</script>

<template>
    <div class="space-y-4 mb-2">
        <!-- 검색 조건 -->
        <SearchThreeCols
            :columns="materialStore.searchColumns"
            v-model:searchData="searchData"
            :formButtons="materialStore.purchaseFormButtons"
            @search="handleSearch"
            @reset="handleReset"
        />
    </div>
    <div>
        <h2>자재 적재 대기 목록 ({{ filteredStorageList.length }}건)</h2>
        <!-- 적재 대기 자재 테이블 -->
        <InputTable 
            :data="filteredStorageList" 
            :columns="tableColumns" 
            :buttons="tableButtons"
            :enableRowActions="false"
            :enableSelection="true"
            selectionMode="multiple"
            :dataKey="'id'"
            title=""
            :showRowCount="false"
            :scrollHeight="'500px'"
        />
    </div>
</template>