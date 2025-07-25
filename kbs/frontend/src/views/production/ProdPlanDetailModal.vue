<!-- views/production/ProdPlanDetailModal.vue -->
<script setup>
import { ref, watch } from 'vue'
import Dialog from 'primevue/dialog'
import BasicTable from '@/components/kimbap/table/BasicTable.vue'

const props = defineProps({
  visible: Boolean,
  detailList: Array,
  title: String
});
</script>

<template>
  <Dialog v-model:visible="visible" :modal="true" :header="title" :style="{ width: '60vw' }">
    <BasicTable
      :data="detailList"
      :columns="columns"
      :scrollHeight="'400px'"
      :selection="[]"
      selectionMode="none"
      dataKey="ppdcode"
    />
  </Dialog>
</template>