<script setup>
import { ref, watch, computed } from 'vue'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import BasicTable from '@/components/kimbap/table/BasicTable.vue' // 기존 사용

const props = defineProps({
  visible: Boolean,
  productList: Object, // 전체 제품 목록
})

const emit = defineEmits(['update:visible', 'select'])

const searchKeyword = ref('')
const filteredList = computed(() => {
  return props.productList.value?.filter(item =>
    item.pcode.includes(searchKeyword.value) ||
    item.prodName.includes(searchKeyword.value)
  )
})

const onSelect = (row) => {
  emit('select', row)
  emit('update:visible', false)
}
</script>

<template>
  <Dialog
    :visible="visible"
    @update:visible="emit('update:visible', $event)"
    modal
    header="제품 선택"
    :style="{ width: '60vw' }"
  >
    <div class="flex gap-2 mb-2">
      <InputText v-model="searchKeyword" placeholder="제품코드 또는 제품명 검색" class="w-full" />
    </div>
    <BasicTable
      :data="filteredList"
      :columns="[
        { field: 'pcode', header: '제품코드' },
        { field: 'prodName', header: '제품명' },
        { field: 'unitName', header: '단위' }
      ]"
      @rowDblclick="onSelect"
      dataKey="pcode"
      selectionMode="single"
    />
  </Dialog>
</template>