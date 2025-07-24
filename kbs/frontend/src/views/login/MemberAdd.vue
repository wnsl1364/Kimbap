<script setup>
import { ref, onBeforeMount, onMounted, computed } from 'vue';
import { storeToRefs } from 'pinia';
import { useStandardMatStore } from '@/stores/standardMatStore';
import InputForm from '@/components/kimbap/searchform/inputForm.vue';

// 1) Pinia 스토어: 전역 데이터와 액션만
const store = useStandardMatStore();
const { fetchMaterials, fetchSuppliers, addMaterial } = store;

// 2) 컴포넌트 레벨 UI 설정
const inputColumns = ref([]);
const inputFormButtons = ref({});

// 4) 라이프사이클에서 UI 설정 및 데이터 로드
onBeforeMount(() => {
    // ─ InputForm 컬럼
    inputColumns.value = [
        {
            key: 'memberType',
            label: '회원유형',
            type: 'dropdown',
            options: [
                { label: '일반사원', value: 'p1' },
                { label: '담당자', value: 'p4' },
                { label: '매출업체', value: 'p2' },
                { label: '공급업체', value: 'p3' },
            ]
        },
        {
            key: 'deptType',
            label: '부서선택',
            type: 'dropdown',
            options: [
                { label: '영업부', value: 'DEPT-1' },
                { label: '생산부', value: 'DEPT-2' },
                { label: '회계부', value: 'DEPT-3' },
                { label: '물류부', value: 'DEPT-4' },
            ]
        },
        {
            key: 'teamType',
            label: '팀선택',
            type: 'dropdown',
            options: [
                { label: '영업팀', value: 'DEPT-1-1' },
                { label: '구매팀', value: 'DEPT-1-2' },
                { label: '생산팀', value: 'DEPT-2-1' },
                { label: '회계팀', value: 'DEPT-3-1' },
                { label: '자재팀', value: 'DEPT-4-1' },
                { label: '물류팀', value: 'DEPT-4-2' },
                { label: '창고1팀', value: 'DEPT-4-3' },
                { label: '창고2팀', value: 'DEPT-4-4' },
            ]
        },
        { key: 'regDt', label: '입사날짜', type: 'calendar', defaultValue: new Date().toISOString().slice(0, 10), },
        { key: 'id', label: '아이디', type: 'text' },
        { key: 'pw', label: '비밀번호', type: 'text' },
        { key: 'id', label: '사원이름', type: 'text' },
        { key: 'id', label: '연락처', type: 'tel' },
        { key: 'id', label: '이메일', type: 'email' },
        { key: 'id', label: '생년월일', type: 'text' },
        { key: 'id', label: '주소', type: 'text' },
    ];
    // ─ 입력폼 버튼
    inputFormButtons.value = {
        save: { show: true, label: '회원등록', severity: 'success' }
    };
});

onMounted(() => {
});

// 5) 이벤트 핸들러
const handleSaveMaterial = async (formData) => {
    const res = await addMaterial(formData);
    alert(res.success ? '등록 성공' : '등록 실패: ' + res.message);
};
</script>

<template>

    <div class="flex flex-col md:flex-row gap-4 mt-6">
        <div class="w-full md:basis-[45%]">
            <InputForm title="자재정보" :columns="inputColumns" :buttons="inputFormButtons" @submit="handleSaveMaterial" />
        </div>
    </div>
</template>
