<template>
  <div class="space-y-4">
    <Labeldinputbar
      label="이름"
      v-model="name"
      placeholder="이름을 입력하세요"
    />

    <Labeldinputbar
      label="이메일"
      v-model="email"
      type="email"
      placeholder="example@example.com"
    />
  </div>
</template>
<script setup>
import Labeldinputbar from '@/components/kimbap/labeldinputbar/labeldinputbar.vue';


</script>