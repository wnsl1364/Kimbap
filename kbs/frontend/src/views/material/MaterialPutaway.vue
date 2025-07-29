<script setup>
import InputForm from '@/components/kimbap/searchform/inputForm.vue';

</script>
<template>
  <div>
    <InputForm 
      v-model="formData"
      :columns="materialStore.inMaterialColumns"
      title="기본정보"
      :buttons="{ 
          save: { show: true, label: '적재처리', severity: 'success' }, 
          reset: { show: false, label: '초기화', severity: 'secondary' },
          delete: { show: false, label: '삭제', severity: 'danger' },
          load: { show: false, label: '불러오기', severity: 'info' }
      }"
      
    />
  </div>
    
</template>