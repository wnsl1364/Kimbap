<!-- views/production/ProdRequestDetailModal.vue -->
<script setup>
import Dialog from 'primevue/dialog'
import BasicTable from '@/components/kimbap/table/BasicTable.vue'
import { defineProps, defineEmits } from 'vue'

const props = defineProps({
  visible: Boolean,
  title: String,
  detailList: Array,  
  columns: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:visible'])
</script>

<template>
  <Dialog
    :visible="visible"
    @update:visible="emit('update:visible', $event)"
    :modal="true"
    :header="title"
    :style="{ width: '60vw' }"
  >
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
