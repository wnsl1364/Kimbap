<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-100">
    <div class="bg-white rounded-2xl shadow-xl w-96 p-6">
      <!-- 로고 영역 -->
      <div class="flex items-center justify-center mb-6">
        <img src="" alt="전북대학교 로고" class="h-10 mr-2" />
        <div class="text-left">
          <h1 class="text-lg font-bold text-blue-800 leading-none">전북대학교</h1>
          <p class="text-sm text-blue-500 leading-none">치과대학</p>
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
import { createApp } from 'vue'
import { ref } from 'vue'
import axios from 'axios';
import { useRouter } from 'vue-router'
import { useMemberStore } from '@/stores/memberStore';

// 라우터
const router = useRouter();

// store 로그인정보 저장용
const memberStore = useMemberStore();

// login
const id = ref('');
const pw = ref('');
const error = ref('');
const rememberId = ref(false) // 아이디 저장 여부

async function handleLogin() {
  // 프론트단 유효성 검사
  if (!id.value) {
    error.value = '아이디를 입력하세요.';
    return;
  }

  if (!pw.value) {
    error.value = '비밀번호를 입력하세요.';
    return;
  }

  try {
    const response = await axios.post('/api/login', {
      id: id.value,
      pw: pw.value,
    });

    // 응답 { token: "...", user: { id, memType, ... } }
    const { token, user } = response.data;

  if (user && user.id) {
      delete user.pw;

      memberStore.saveUser(user);                 // Pinia 저장
      sessionStorage.setItem('member', JSON.stringify(user)); // 세션 저장
      localStorage.setItem('token', token);       // 토큰은 로컬 저장

      console.log('저장된 정보:', user);
      console.log('저장된 토큰:', token);

      // 메인페이지로 이동
      router.push('/');
    } else {
      error.value = '로그인 응답이 올바르지 않습니다.';
    }
  } catch (err) {
    console.error(' 로그인 오류:', err);
    error.value = '사원번호와 비밀번호를 확인해주세요.';
  }
}
// logout() {
//   this.user = null;
//   sessionStorage.removeItem('member');
//   localStorage.removeItem('token');
// }
</script>

<style scoped>
/* 추후 필요시 커스텀 스타일 추가 가능 */
</style>

