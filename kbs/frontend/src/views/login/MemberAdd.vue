<script setup>
import { ref, computed, onMounted } from 'vue';
import RadioButton from 'primevue/radiobutton';
import InputForm from '@/components/kimbap/searchform/inputForm.vue';
import { useToast } from 'primevue/usetoast';
const toast = useToast();

// 반응형 데이터
const userType = ref('internal'); // 'internal' 또는 'supplier'

// 검색 컬럼 설정 (권한별로 다름!)
const inputColumns = computed(() => {
  if (userType.value === 'internal') {
    // 내부직원용 검색 조건
    return [
        {
            key: 'employee',
            label: '사원이름',
            type: 'dropdown',
            options: [
                { label: '데이터1', value: 'p1' },
                { label: '데이터2', value: 'p4' },
            ]
        },
        {
            key: 'memberType',
            label: '회원 유형',
            type: 'dropdown',
            options: [
                { label: '사원', value: 'p1' },
                { label: '담당자', value: 'p4' },
            ],
            disabled: (form) => form.memberType === 'p2' || form.memberType === 'p3'
        },
        { key: 'regDt', label: '등록일자', type: 'calendar' },
        { key: 'id', label: '아이디', type: 'text' },
        { key: 'pw', label: '비밀번호', type: 'text' },
    ];
  } else {
    // 공급업체직원용 검색 조건
    return [
        {
            key: 'company',
            label: '거래처명',
            type: 'dropdown',
            options: [
                { label: '매출업체', value: 'p2' },
                { label: '공급업체', value: 'p3' },
            ]
        },
        {
            key: 'memberType',
            label: '회원 유형',
            type: 'dropdown',
            options: [
                { label: '매출업체', value: 'p2' },
                { label: '공급업체', value: 'p3' },
            ],
            disabled: (form) => form.memberType === 'p2' || form.memberType === 'p3'
        },
        { key: 'regDt', label: '등록일자', type: 'calendar' },
        { key: 'id', label: '아이디', type: 'text' },
        { key: 'pw', label: '비밀번호', type: 'text' },
    ];
  }
});

</script>

<template>
  <div class="grid">
    <div class="col-12">
      <div class="card">
        <h5>계정 등록</h5>
        
        <!-- 사용자 타입 선택 -->
        <div class="mb-4">
          <div class="field-radiobutton">
            <RadioButton v-model="userType" inputId="internal" value="internal" />
            <label for="internal" class="ml-2">내부직원</label>
          </div>
          <div class="field-radiobutton">
            <RadioButton v-model="userType" inputId="supplier" value="supplier" />
            <label for="supplier" class="ml-2">공급업체직원</label>
          </div>
        </div>
            <div class="flex flex-col md:flex-row gap-4 mt-6">
        <div class="w-full md:basis-[45%]">
            <InputForm
                title=""
                :columns="inputColumns"
                :buttons="inputFormButtons"
                :formData="formData"
                :isReadonly="isReadonly"
                @submit="handleSaveMaterial"
            />
        </div>
    </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.field-radiobutton {
  display: inline-flex;
  align-items: center;
  margin-right: 1rem;
}
</style>