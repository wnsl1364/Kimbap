<template>
    <Dialog
    :visible="visible"
    @update:visible="emit('update:visible', $event)"
    modal
    header="상품 선택"
    :style="{ width: '60rem' }"
    :closable="false"
    >

    <!-- 🔍 검색창 -->
    <div class="mb-4">
      <InputText
        v-model="searchText"
        placeholder="상품명을 입력하세요"
        class="w-full"
      />
    </div>

    <!-- 📋 테이블 (단일 선택 + 라디오 컬럼) -->
    <DataTable
      :value="filteredProducts"
      v-model:selection="selectedProduct"
      selectionMode="single"
      dataKey="code"
      tableStyle="min-width: 50rem"
    >
      <!-- ✅ 단일 선택용 라디오 컬럼 -->
      <Column selectionMode="single" headerStyle="width: 3rem" />

      <Column field="code" header="Code" />
      <Column field="name" header="Name" />
      <Column field="category" header="Category" />
      <Column field="quantity" header="Quantity" />
    </DataTable>

    <!-- 하단 버튼 -->
    <div class="flex justify-end gap-2 mt-4">
      <Button label="닫기" severity="secondary" @click="onClose" />
      <Button label="확인" @click="onConfirm" :disabled="!selectedProduct" />
    </div>
  </Dialog>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ProductService } from '@/service/ProductService'
import Dialog from 'primevue/dialog'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import InputText from 'primevue/inputtext'
import Button from 'primevue/button'

const props = defineProps({
  visible: Boolean,
})

const emit = defineEmits(['update:visible', 'update:modelValue'])

const searchText = ref('')
const products = ref([])
const selectedProduct = ref(null)

onMounted(async () => {
  const res = await ProductService.getProductsMini()
  products.value = res
})

const filteredProducts = computed(() => {
  if (!searchText.value) return products.value
  return products.value.filter((item) =>
    item.name.toLowerCase().includes(searchText.value.toLowerCase())
  )
})

function onClose() {
  emit('update:visible', false)
}

function onConfirm() {
  emit('update:modelValue', selectedProduct.value)
  emit('update:visible', false)
}
</script>
