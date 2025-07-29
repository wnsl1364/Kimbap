<script setup>
import { ref, computed, onMounted, onBeforeMount } from 'vue';
import RadioButton from 'primevue/radiobutton';
import InputForm from '@/components/kimbap/searchform/inputForm.vue';
import { empListCheck, cpListCheck, saveMemberAdd } from '@/api/member';

// 추가할 부분
const isReadonly = ref(false);

// 드롭다운 옵션
const empOptions = ref([]); // 사원
const cpOptions = ref([]);  // 거래처

// 폼 데이터
const formData = ref({
  employee: '',
  company: '',
  memberType: '',
  regDt: '',
  id: '',
  pw: ''
});

// 버튼 정의
const inputFormButtons = {
  save: { show: true, label: '등록', severity: 'success' },
  reset: { show: true, label: '초기화', severity: 'secondary' },
  delete: { show: false },
  load: { show: false }
};

const handleSaveMember = async () => {
try {
  await saveMemberAdd(formData.value);
  alert('✅ 등록 성공!');
} catch (err) {
  if (err.response && err.response.status === 409) {
    alert('❗이미 존재하는 아이디입니다.');
  } else {
    alert('⚠️ 등록 중 오류 발생: ' + (err.response?.data || err.message));
  }
}
};

// 반응형 데이터
const userType = ref('internal'); // 'internal' 또는 'supplier'

// 검색 컬럼 설정 (권한별로 다름!)
const inputColumns = computed(() => {
  if (userType.value === 'internal') {
    // 내부직원 등록
    return [
        {
            key: 'empCd',
            label: '사원이름',
            type: 'dropdown',
            options: empOptions.value // ❗ 이미 label/value 형태임
      },
        {
            key: 'memType',
            label: '회원 유형',
            type: 'dropdown',
            options: [
                { label: '사원', value: 'p1' },
                { label: '담당자', value: 'p4' },
            ]
        },
        { key: 'id', label: '아이디', type: 'text' },
        { key: 'pw', label: '비밀번호', type: 'text' },
    ];
  } else {
    // 업체계정 등록
    return [
        {
            key: 'cpCd',
            label: '거래처명',
            type: 'dropdown',
            options: cpOptions.value
        },
        {
            key: 'memType',
            label: '회원 유형',
            type: 'dropdown',
            options: [
                { label: '매출업체', value: 'p2' },
                { label: '공급업체', value: 'p3' },
            ]
        },
        { key: 'id', label: '아이디', type: 'text' },
        { key: 'pw', label: '비밀번호', type: 'text' },
    ];
  }
});

// mount 시점에 데이터 불러오기
onBeforeMount(async () => {
  try {
    const empRes = await empListCheck();
    empOptions.value = empRes.data.map(emp => ({
          label: emp.empName,
          value: emp.empCd
        })) ;
    console.log('사원 목록:', empOptions.value);

    const cpRes = await cpListCheck();
        cpOptions.value = cpRes.data.map(cp => ({
          label: cp.cpName,
          value: cp.cpCd
        })) ;
    console.log('거래처 목록:', cpOptions.value);
  } catch (err) {
    console.error('데이터 불러오기 실패:', err);
  }
});
</script>

<template>
  <div class="flex justify-center pt-20 min-h-screen bg-gray-50">
    <div class="w-full max-w-[800px] px-4">
      <div class="card p-6">
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
          <div class="w-full md:basis-[45%]">
            <InputForm
              title=""
              v-model:data="formData"
              :columns="inputColumns"
              :buttons="inputFormButtons"
              :formData="formData"
              :isReadonly="isReadonly"
              @submit="handleSaveMember"
            />
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