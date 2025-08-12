<template>
  <!-- ✅ 이 페이지 전용 토스트 컨테이너 -->
  <Toast position="top-right" />

  <div class="min-h-screen flex items-center justify-center bg-gray-100">
    <div class="bg-white rounded-2xl shadow-xl w-96 p-6">
      <!-- 로고 영역 -->
      <div class="flex items-center justify-center mb-6">
        <img src="../../image/kimbab.png" alt="김밥 로고" class="h-20 mr-2" />
        <div class="text-left">
          <p class="text-sm text-blue-500 leading-none mb-3">Login</p>
          <h1 class="text-lg font-bold text-blue-800 leading-none mt-2">잘말아조</h1>
        </div>
      </div>

      <!-- 로그인 폼 -->
      <!-- ✅ form 으로 감싸고 submit 이벤트에 handleLogin 연결 -->
      <form @submit.prevent="handleLogin">
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

        <div class="flex items-center mb-4"></div>

        <!-- ✅ 버튼을 submit 타입으로 -->
        <button
          type="submit"
          class="w-full bg-blue-500 hover:bg-blue-600 text-white py-2 rounded"
        >
          로그인
        </button>
      </form>
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
