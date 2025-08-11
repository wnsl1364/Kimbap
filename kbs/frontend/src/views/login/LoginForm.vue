<template>
  <!-- ✅ 이 페이지 전용 토스트 컨테이너 -->
  <Toast position="top-right" />

  <div class="min-h-screen flex items-center justify-center bg-gray-100">
    <div class="bg-white rounded-2xl shadow-xl w-96 p-6">
      <!-- 로고 영역 -->
      <div class="flex items-center justify-center mb-6">
        <img src="" alt="김밥 로고" class="h-10 mr-2" />
        <div class="text-left">
          <h1 class="text-lg font-bold text-blue-800 leading-none">잘</h1>
          <p class="text-sm text-blue-500 leading-none">말아조</p>
        </div>
      </div>

      <!-- 로그인 폼 -->
      <div>
        <input
          v-model="id"
          type="text"
          placeholder="ID"
          class="w-full mb-3 px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring focus:ring-blue-300"
        />
        <input
          v-model="pw"
          type="password"
          placeholder="PW"
          class="w-full mb-3 px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring focus:ring-blue-300"
        />

        <div class="flex items-center mb-4">
          <input
            v-model="rememberId"
            type="checkbox"
            id="remember"
            class="accent-blue-500"
          />
          <label for="remember" class="ml-2 text-sm text-gray-600">아이디 저장</label>
        </div>

        <button
          @click="handleLogin"
          class="w-full bg-blue-500 hover:bg-blue-600 text-white py-2 rounded"
        >
          로그인
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios';
import { useRouter } from 'vue-router'
import { useMemberStore } from '@/stores/memberStore';
import { useToast } from 'primevue/usetoast'
import Toast from 'primevue/toast'

const router = useRouter();
const memberStore = useMemberStore();
const toast = useToast();

const id = ref('');
const pw = ref('');
const rememberId = ref(false);

// 저장된 아이디 복원
onMounted(() => {
  const saved = localStorage.getItem('rememberedId');
  if (saved) {
    id.value = saved;
    rememberId.value = true;
  }
});

async function handleLogin() {
  if (!id.value) {
    toast.add({ severity: 'warn', summary: '입력 필요', detail: '아이디를 입력하세요.', life: 2500 });
    return;
  }
  if (!pw.value) {
    toast.add({ severity: 'warn', summary: '입력 필요', detail: '비밀번호를 입력하세요.', life: 2500 });
    return;
  }

  try {
    const response = await axios.post('/api/login', { id: id.value, pw: pw.value });
    const { token, user } = response.data;

    if (user && user.id) {
      delete user.pw;
      memberStore.saveUser(user);
      sessionStorage.setItem('member', JSON.stringify(user));
      localStorage.setItem('token', token);

      if (rememberId.value) localStorage.setItem('rememberedId', id.value);
      else localStorage.removeItem('rememberedId');

      toast.add({ severity: 'success', summary: '로그인 성공', detail: `${user.id}님 환영합니다.`, life: 2000 });
      router.push('/');
    } else {
      toast.add({ severity: 'error', summary: '로그인 오류', detail: '로그인 응답이 올바르지 않습니다.', life: 3000 });
    }
  } catch (err) {
    console.error('로그인 오류:', err);
    toast.add({ severity: 'error', summary: '로그인 실패', detail: '사원번호와 비밀번호를 확인해주세요.', life: 3000 });
  }
}
</script>

<style scoped>
/* 필요 시 추가 스타일 */
</style>
