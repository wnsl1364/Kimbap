<template>
    <div class="material-stock-view">
        <h1>자재 재고 조회</h1>
        
        <!-- TableComponent 사용 예시 -->
        <TableComponent 
            :searchColumns="searchColumns"
            :allowDynamicColumns="true"
        />
    </div>
</template>

<script setup>
import { ref } from 'vue';
import TableComponent from '@/components/kimbap/TableComponent.vue';

// 동적 검색 컬럼 설정 (text, text, radio, calendar 순서)
const searchColumns = ref([
    {
        key: 'materialName',
        label: '자재명',
        type: 'text',
        placeholder: '자재명을 입력하세요'
    },
    {
        key: 'location',
        label: '위치',
        type: 'text',
        placeholder: '위치를 입력하세요'
    },
    {
        key: 'status',
        label: '상태',
        type: 'radio',
        options: [
            { label: '정상', value: 'normal' },
            { label: '부족', value: 'shortage' },
            { label: '과재고', value: 'overstock' }
        ]
    },
    {
        key: 'lastUpdated',
        label: '최종 업데이트',
        type: 'calendar',
        placeholder: '날짜를 선택하세요'
    }
]);
</script>

<style scoped>
.material-stock-view {
    padding: 20px;
}
</style>