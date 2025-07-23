<script setup>
import { onMounted } from 'vue'
import { useMemberStore } from '@/stores/memberStore'

const memberStore = useMemberStore();

// pinia에 사용자 정보가 없을 경우 세션 스토리지에서 가져와 동기화 해줌
onMounted(() => {
  if (!memberStore.user) {
    const userStr = sessionStorage.getItem('member');
    if (userStr) {
      memberStore.saveUser(JSON.parse(userStr));
    }
  }
});
</script>

<template>
    <router-view />
</template>

<style scoped></style>
