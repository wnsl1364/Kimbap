<script setup>
import { ref, computed, onMounted, onBeforeMount } from 'vue'
import RadioButton from 'primevue/radiobutton'
import InputForm from '@/components/kimbap/searchform/inputForm.vue'
import { empListCheck, cpListCheck, saveMemberAdd } from '@/api/member'
import { useToast } from 'primevue/usetoast'
import Toast from 'primevue/toast' // ✅ 페이지 전용 컨테이너

const toast = useToast()

// 추가할 부분
const isReadonly = ref(false)

// 드롭다운 옵션
const empOptions = ref([]) // 사원
const cpOptions = ref([])  // 거래처

// 폼 데이터
const formData = ref({
  employee: '',
  company: '',
  memberType: '',
  regDt: '',
  id: '',
  pw: ''
})

// 버튼 정의
const inputFormButtons = {
  save: { show: true, label: '등록', severity: 'success' },
  reset: { show: true, label: '초기화', severity: 'secondary' },
  delete: { show: false },
  load: { show: false }
}

// 반응형 데이터
const userType = ref('internal') // 'internal' | 'supplier'

// 검색 컬럼 설정 (권한별로 다름)
const inputColumns = computed(() => {
  if (userType.value === 'internal') {
    return [
      {
        key: 'empCd',
        label: '사원이름',
        type: 'dropdown',
        options: empOptions.value
      },
      {
        key: 'memType',
        label: '회원 유형',
        type: 'dropdown',
        options: [
          { label: '사원', value: 'p1' },
          { label: '담당자', value: 'p4' }
        ]
      },
      { key: 'id', label: '아이디', type: 'text' },
      { key: 'pw', label: '비밀번호', type: 'text' }
    ]
  } else {
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
          { label: '공급업체', value: 'p3' }
        ]
      },
      { key: 'id', label: '아이디', type: 'text' },
      { key: 'pw', label: '비밀번호', type: 'text' }
    ]
  }
})

// ✅ 토스트로 바꾼 저장 로직 + 간단 유효성 검사
const handleSaveMember = async () => {
  // 필수값 체크
  const missing = []
  if (userType.value === 'internal') {
    if (!formData.value.empCd) missing.push('사원이름')
  } else {
    if (!formData.value.cpCd) missing.push('거래처명')
  }
  if (!formData.value.memType) missing.push('회원 유형')
  if (!formData.value.id) missing.push('아이디')
  if (!formData.value.pw) missing.push('비밀번호')

  if (missing.length) {
    toast.add({
      severity: 'warn',
      summary: '입력 필요',
      detail: `${missing.join(', ')} 항목을 입력하세요.`,
      life: 3000
    })
    return
  }

  try {
    await saveMemberAdd(formData.value)
    toast.add({
      severity: 'success',
      summary: '등록 완료',
      detail: '계정이 정상 등록되었습니다.',
      life: 2000
    })
    // 필요 시 폼 초기화
    // Object.assign(formData.value, { employee:'', company:'', memberType:'', regDt:'', id:'', pw:'' })
  } catch (err) {
    if (err?.response?.status === 409) {
      toast.add({
        severity: 'error',
        summary: '중복 아이디',
        detail: '이미 존재하는 아이디입니다.',
        life: 3000
      })
    } else {
      toast.add({
        severity: 'error',
        summary: '등록 실패',
        detail: err?.response?.data?.message || err?.response?.data || err.message,
        life: 3500
      })
    }
  }
}

// mount 시점에 데이터 불러오기
onBeforeMount(async () => {
  try {
    const empRes = await empListCheck()
    empOptions.value = empRes.data.map(emp => ({
      label: emp.empName,
      value: emp.empCd
    }))

    const cpRes = await cpListCheck()
    cpOptions.value = cpRes.data.map(cp => ({
      label: cp.cpName,
      value: cp.cpCd
    }))
  } catch (err) {
    console.error('데이터 불러오기 실패:', err)
    toast.add({
      severity: 'error',
      summary: '조회 실패',
      detail: '사원/거래처 목록 조회 중 오류가 발생했습니다.',
      life: 3000
    })
  }
})
</script>

<template>
  <!-- ✅ 이 페이지 전용 토스트 컨테이너 (App.vue 수정 불필요) -->
  <Toast position="top-right" />

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
