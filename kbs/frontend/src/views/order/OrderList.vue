<template>
  <div class="space-y-4">    
    <BasicTable
      :data="products"
      :columns="productColumns"/>
  </div>    
</template>
<script setup>
import BasicTable from '@/components/kimbap/table/BasicTable.vue'
</script>