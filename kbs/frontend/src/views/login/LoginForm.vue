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
import { createPinia } from 'pinia'

// pinia
const pinia = createPinia()
// const app = createApp(App)

// login
const id = ref('')
const pw = ref('')
const error = ref('')
const rememberId = ref(false) // 아이디 저장 여부

console.log(id, pw);

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

  // 서버에 요청
  // 로그인 정보 필요할시 아래 코드 user.value 로 접근가능
  try {
    const response = await axios.post('/api/login', {
      id: id.value,
      pw: pw.value,
    });

    if (response.data.success) {
      const userData = response.data; // 지역변수
      store.dispatch('saveUser', response.data.user); // saveUser 액션에 로그인정보 넘겨서 전역 state.user 에 로그인정보 저장
      sessionStorage.setItem('user', JSON.stringify({
        id: userData.id,
        empName: userData.empName,
        tel: userData.tel
      })); // 로컬스토리지에 user라는 키 이름으로 저장 (새로고침 시 로그인정보 유지)
      router.push('/');
    } else {
      error.value = response.data.message;
    }
  } catch (err) {
    console.error('로그인 오류:', err);
    error.value = '사원번호와 비밀번호를 확인해주세요.';
  }
}

</script>

<style scoped>
/* 추후 필요시 커스텀 스타일 추가 가능 */
</style>