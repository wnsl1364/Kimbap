<!-- MrpPreviewModal.vue -->
<script setup>
import { ref, computed } from 'vue'
import Dialog from 'primevue/dialog'
import Button from 'primevue/button'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import Card from 'primevue/card'
import Tag from 'primevue/tag'
import { format } from 'date-fns'
import { ko } from 'date-fns/locale'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  mrpPreview: {
    type: Object,
    default: () => ({})
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:visible', 'confirm', 'cancel'])

// 모달 닫기
const closeModal = () => {
  emit('update:visible', false)
}

// 생산계획 등록 확인
const handleConfirm = () => {
  emit('confirm')
  closeModal()
}

// 취소
const handleCancel = () => {
  emit('cancel')
  closeModal()
}

// MRP 정보 계산
const mrpSummary = computed(() => {
  if (!props.mrpPreview.mrpDetails) return {}
  
  const totalItems = props.mrpPreview.mrpDetails.length
  const totalRequiredQty = props.mrpPreview.mrpDetails.reduce((sum, item) => 
    sum + (parseFloat(item.requiredQty) || 0), 0
  )
  
  return {
    totalItems,
    totalRequiredQty: totalRequiredQty.toFixed(2)
  }
})

// 발주서 정보 계산
const purchaseOrderSummary = computed(() => {
  if (!props.mrpPreview.purchaseOrderDetails) return {}
  
  const totalItems = props.mrpPreview.purchaseOrderDetails.length
  const totalAmount = props.mrpPreview.purchaseOrderDetails.reduce((sum, item) => 
    sum + (parseFloat(item.totalAmount) || 0), 0
  )
  const totalQty = props.mrpPreview.purchaseOrderDetails.reduce((sum, item) => 
    sum + (parseFloat(item.purcQty) || 0), 0
  )
  
  return {
    totalItems,
    totalAmount: totalAmount.toLocaleString(),
    totalQty: totalQty.toFixed(2)
  }
})

// 날짜 포맷팅
const formatDate = (date) => {
  if (!date) return '-'
  return format(new Date(date), 'yyyy-MM-dd', { locale: ko })
}

// 금액 포맷팅
const formatCurrency = (amount) => {
  if (!amount) return '0원'
  return Math.round(parseFloat(amount)).toLocaleString() + '원'
}

// 수량 포맷팅
const formatQuantity = (qty, unit) => {
  if (!qty) return '0'
  return Math.round(parseFloat(qty)).toLocaleString() + (unit ? ` ${unit}` : '')
}
</script>

<template>
  <Dialog
    :visible="visible"
    @update:visible="closeModal"
    modal
    :style="{ width: '90vw', maxWidth: '1200px' }"
    :closable="true"
    :draggable="false"
    position="center"
  >
    <template #header>
      <div class="flex items-center gap-3">
        <i class="pi pi-eye text-blue-600"></i>
        <span class="text-xl font-semibold text-gray-800">생산계획 실행 미리보기</span>
      </div>
    </template>

    <div v-if="loading" class="flex justify-center items-center h-64">
      <div class="text-center">
        <i class="pi pi-spin pi-spinner text-4xl text-blue-500 mb-4"></i>
        <p class="text-gray-600">MRP 및 발주서 정보를 계산 중입니다...</p>
      </div>
    </div>

    <div v-else class="space-y-6">
      <!-- 요약 정보 카드 -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <!-- MRP 요약 -->
        <Card>
          <template #title>
            <div class="flex items-center gap-2">
              <i class="pi pi-sitemap text-orange-600"></i>
              <span class="text-lg">MRP 요약</span>
            </div>
          </template>
          <template #content>
            <div class="space-y-3">
              <div class="flex justify-between">
                <span class="text-gray-600">부족 자재 종류:</span>
                <Tag :value="`${mrpSummary.totalItems}개`" severity="warning" />
              </div>
              <div class="flex justify-between">
                <span class="text-gray-600">총 부족 수량:</span>
                <span class="font-semibold">{{ formatQuantity(mrpSummary.totalRequiredQty) }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-600">MRP 생성일:</span>
                <span>{{ formatDate(new Date()) }}</span>
              </div>
            </div>
          </template>
        </Card>

        <!-- 발주서 요약 -->
        <Card>
          <template #title>
            <div class="flex items-center gap-2">
              <i class="pi pi-shopping-cart text-green-600"></i>
              <span class="text-lg">발주서 요약</span>
            </div>
          </template>
          <template #content>
            <div class="space-y-3">
              <div class="flex justify-between">
                <span class="text-gray-600">발주 자재 종류:</span>
                <Tag :value="`${purchaseOrderSummary.totalItems}개`" severity="success" />
              </div>
              <div class="flex justify-between">
                <span class="text-gray-600">총 발주 수량:</span>
                <span class="font-semibold">{{ formatQuantity(purchaseOrderSummary.totalQty) }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-600">총 발주 금액:</span>
                <span class="font-semibold text-green-600">{{ purchaseOrderSummary.totalAmount }}원</span>
              </div>
            </div>
          </template>
        </Card>
      </div>

      <!-- 탭 뷰 -->
      <TabView>
        <!-- MRP 상세 탭 -->
        <TabPanel>
          <template #header>
            <div class="flex items-center gap-2">
              <i class="pi pi-sitemap"></i>
              <span>MRP 상세 내역</span>
              <Tag :value="mrpSummary.totalItems" severity="warning" rounded />
            </div>
          </template>

          <DataTable 
            :value="mrpPreview.mrpDetails || []" 
            :paginator="true" 
            :rows="10"
            scrollable 
            scrollHeight="400px"
            class="mt-4"
          >
            <Column field="mcode" header="자재코드" style="min-width: 150px">
              <template #body="{ data }">
                <span class="font-mono text-sm">{{ data.mcode }}</span>
              </template>
            </Column>
            <Column field="mateName" header="자재명" style="min-width: 250px">
              <template #body="{ data }">
                <span class="font-medium">{{ data.mateName || '-' }}</span>
              </template>
            </Column>
            <Column field="requiredQty" header="부족 수량" style="min-width: 150px" class="text-right">
              <template #body="{ data }">
                <span class="text-red-600 font-semibold">
                  {{ formatQuantity(data.requiredQty) }}
                </span>
              </template>
            </Column>
            <Column field="currentStock" header="현재고" style="min-width: 150px" class="text-right">
              <template #body="{ data }">
                <span class="text-blue-600">
                  {{ formatQuantity(data.currentStock) }}
                </span>
              </template>
            </Column>
          </DataTable>
        </TabPanel>

        <!-- 발주서 상세 탭 -->
        <TabPanel>
          <template #header>
            <div class="flex items-center gap-2">
              <i class="pi pi-shopping-cart"></i>
              <span>발주서 상세 내역</span>
              <Tag :value="purchaseOrderSummary.totalItems" severity="success" rounded />
            </div>
          </template>

          <DataTable 
            :value="mrpPreview.purchaseOrderDetails || []" 
            :paginator="true" 
            :rows="10"
            scrollable 
            scrollHeight="400px"
            class="mt-4"
          >
            <Column field="mcode" header="자재코드" style="min-width: 120px">
              <template #body="{ data }">
                <span class="font-mono text-sm">{{ data.mcode }}</span>
              </template>
            </Column>
            <Column field="mateName" header="자재명" style="min-width: 180px">
              <template #body="{ data }">
                <span class="font-medium">{{ data.mateName || '-' }}</span>
              </template>
            </Column>
            <Column field="supplierName" header="공급업체" style="min-width: 150px">
              <template #body="{ data }">
                <span class="text-blue-600">{{ data.supplierName || '-' }}</span>
              </template>
            </Column>
            <Column field="purcQty" header="발주수량" style="min-width: 120px" class="text-right">
              <template #body="{ data }">
                <span class="font-semibold">
                  {{ formatQuantity(data.purcQty) }}
                </span>
              </template>
            </Column>
            <Column field="unitPrice" header="단가" style="min-width: 100px" class="text-right">
              <template #body="{ data }">
                <span>{{ formatCurrency(data.unitPrice) }}</span>
              </template>
            </Column>
            <Column field="totalAmount" header="금액" style="min-width: 120px" class="text-right">
              <template #body="{ data }">
                <span class="font-semibold text-green-600">
                  {{ formatCurrency(data.totalAmount) }}
                </span>
              </template>
            </Column>
            <Column field="exDeliDt" header="납기예정일" style="min-width: 120px" class="text-center">
              <template #body="{ data }">
                <span>{{ formatDate(data.exDeliDt) }}</span>
              </template>
            </Column>
            <Column field="leadTime" header="리드타임" style="min-width: 100px" class="text-center">
              <template #body="{ data }">
                <Tag :value="`${data.leadTime || 0}일`" severity="secondary" rounded />
              </template>
            </Column>
          </DataTable>
        </TabPanel>
      </TabView>

      <!-- 주의사항 -->
      <Card class="bg-yellow-50 border-l-4 border-yellow-400">
        <template #content>
          <div class="flex items-start gap-3">
            <i class="pi pi-exclamation-triangle text-yellow-600 mt-1"></i>
            <div>
              <h4 class="font-semibold text-yellow-800 mb-2">주의사항</h4>
              <ul class="text-sm text-yellow-700 space-y-1">
                <li>• 생산계획 등록 시 위 MRP와 발주서가 자동으로 생성됩니다.</li>
                <li>• 발주서는 최저가 공급업체 기준으로 자동 계산되었습니다.</li>
                <li>• 최소발주단위(MOQ)가 적용되어 실제 발주량이 조정될 수 있습니다.</li>
                <li>• 등록 후 발주 담당자가 발주서를 검토하고 수정할 수 있습니다.</li>
              </ul>
            </div>
          </div>
        </template>
      </Card>
    </div>

    <template #footer>
      <div class="flex justify-end gap-3">
        <Button 
          label="취소" 
          icon="pi pi-times" 
          severity="secondary" 
          @click="handleCancel"
          :disabled="loading"
        />
        <Button 
          label="생산계획 등록" 
          icon="pi pi-check" 
          severity="success" 
          @click="handleConfirm"
          :disabled="loading"
          :loading="loading"
        />
      </div>
    </template>
  </Dialog>
</template>

<style scoped>
:deep(.p-dialog-content) {
  padding: 1.5rem;
}

:deep(.p-tabview-nav-link) {
  padding: 0.75rem 1rem;
}

:deep(.p-datatable .p-datatable-thead > tr > th) {
  background-color: #f8fafc;
  color: #374151;
  font-weight: 600;
}

:deep(.p-card .p-card-title) {
  margin-bottom: 1rem;
  color: #374151;
}
</style>