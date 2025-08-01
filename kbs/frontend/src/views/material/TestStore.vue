<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useToast } from 'primevue/usetoast'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import InputNumber from 'primevue/inputnumber'
import Dropdown from 'primevue/dropdown'
import Card from 'primevue/card'

const toast = useToast()

// 적재 대상 자재
const currentMaterial = ref({
  mateInboCd: 'MATI-202507-0001',
  materialName: '김(건조)',
  lotNo: 'LOT-100-20250801-1',
  totalQuantity: 500, // 총 입고량
  remainingQuantity: 500, // 남은 적재량
  unit: 'kg',
  storageCondition: '상온'
})

// 창고 설정 (30x30 대응)
const warehouseConfig = ref({
  rows: 8, // A-H (테스트용)
  cols: 6,  // 1-6
  floors: 3 // 1-3층
})

// 창고 상태 데이터
const warehouseData = ref({})

// UI 상태
const currentFloor = ref(1)
const selectedZones = ref([])
const placementPlan = ref([])
const showSplitModal = ref(false)
const splitModalData = ref({})
const viewMode = ref('grid') // grid, dropdown, list

// 뷰 모드 옵션
const viewModeOptions = ref([
  { label: '그리드뷰', value: 'grid' },
  { label: '드롭다운', value: 'dropdown' },
  { label: '리스트뷰', value: 'list' }
])

// 층 옵션
const floorOptions = computed(() => {
  return Array.from({ length: warehouseConfig.value.floors }, (_, i) => ({
    label: `${i + 1}층`,
    value: i + 1
  }))
})

// 창고 데이터 초기화
const initializeWarehouse = () => {
  const data = {}
  for (let floor = 1; floor <= warehouseConfig.value.floors; floor++) {
    for (let row = 0; row < warehouseConfig.value.rows; row++) {
      for (let col = 1; col <= warehouseConfig.value.cols; col++) {
        const rowLabel = generateRowLabel(row)
        const zoneId = `${rowLabel}-${col}-${floor}`
        data[zoneId] = {
          capacity: 200,
          occupied: Math.floor(Math.random() * 100),
          materials: Math.random() > 0.7 ? ['김(건조)'] : [],
          temperature: '상온'
        }
      }
    }
  }
  warehouseData.value = data
}

// 행 라벨 생성 (30x30 대응 - A, B, C... Z, AA, AB, AC...)
const generateRowLabel = (rowIndex) => {
  if (rowIndex < 26) {
    return String.fromCharCode(65 + rowIndex) // A-Z
  }
  const firstChar = String.fromCharCode(65 + Math.floor(rowIndex / 26) - 1)
  const secondChar = String.fromCharCode(65 + (rowIndex % 26))
  return firstChar + secondChar // AA, AB, AC...
}

// 칸 크기 계산 (반응형)
const getCellSize = computed(() => {
  const { rows, cols } = warehouseConfig.value
  if (rows > 20 || cols > 20) {
    return { width: '20px', height: '20px', fontSize: '8px' }
  }
  if (rows > 10 || cols > 10) {
    return { width: '35px', height: '35px', fontSize: '10px' }
  }
  return { width: '50px', height: '50px', fontSize: '12px' }
})

// 점유율 계산
const getOccupancyRate = (zoneId) => {
  const zone = warehouseData.value[zoneId]
  if (!zone) return 0
  return Math.round((zone.occupied / zone.capacity) * 100)
}

// 칸 색상 결정
const getCellColor = (zoneId) => {
  const zone = warehouseData.value[zoneId]
  const rate = getOccupancyRate(zoneId)
  const isSelected = selectedZones.value.includes(zoneId)
  const hasSameMaterial = zone?.materials.includes(currentMaterial.value.materialName)

  if (isSelected) return 'bg-blue-500 text-white border-blue-700'
  if (hasSameMaterial) return 'bg-green-200 border-green-400'
  if (rate >= 90) return 'bg-red-200 border-red-400'
  if (rate >= 70) return 'bg-yellow-200 border-yellow-400'
  return 'bg-gray-100 border-gray-300 hover:bg-gray-200'
}

// 칸 클릭 처리
const handleZoneClick = (zoneId) => {
  const zone = warehouseData.value[zoneId]
  const availableCapacity = zone.capacity - zone.occupied

  if (availableCapacity <= 0) {
    toast.add({
      severity: 'warn',
      summary: '용량 부족',
      detail: '해당 구역은 용량이 부족합니다!',
      life: 3000
    })
    return
  }

  // 이미 선택된 구역이면 제거
  if (selectedZones.value.includes(zoneId)) {
    selectedZones.value = selectedZones.value.filter(id => id !== zoneId)
    placementPlan.value = placementPlan.value.filter(p => p.zoneId !== zoneId)
    return
  }

  // 남은 수량이 용량보다 작거나 같으면 전체 적재
  if (currentMaterial.value.remainingQuantity <= availableCapacity) {
    const newPlan = {
      zoneId,
      quantity: currentMaterial.value.remainingQuantity,
      availableCapacity
    }
    
    selectedZones.value.push(zoneId)
    placementPlan.value.push(newPlan)
    currentMaterial.value.remainingQuantity = 0
    
    toast.add({
      severity: 'success',
      summary: '적재 등록 완료',
      detail: `${zoneId}에 ${newPlan.quantity}kg 적재 등록`,
      life: 3000
    })
    
  } else {
    // 용량 초과 → 자동 분할 또는 수동 선택
    handleOverflowSituation(zoneId, availableCapacity)
  }
}

// 용량 초과 상황 처리
const handleOverflowSituation = (zoneId, availableCapacity) => {
  const sameMatZones = Object.keys(warehouseData.value).filter(id => {
    const zone = warehouseData.value[id]
    return zone.materials.includes(currentMaterial.value.materialName) &&
           (zone.capacity - zone.occupied) > 0 &&
           id !== zoneId
  })

  if (sameMatZones.length > 0) {
    // 자동 분할 적재 제안
    const confirm = window.confirm(
      `💡 선택한 구역 용량: ${availableCapacity}kg\n` +
      `필요 용량: ${currentMaterial.value.remainingQuantity}kg\n\n` +
      `같은 자재가 있는 다른 구역에 자동 분할 적재하시겠습니까?\n` +
      `(수동 분할을 원하시면 "취소"를 누르세요)`
    )

    if (confirm) {
      executeAutoSplit(zoneId, availableCapacity, sameMatZones)
    } else {
      showManualSplitModal(zoneId, availableCapacity)
    }
  } else {
    // 같은 자재가 없으면 수동 선택
    showManualSplitModal(zoneId, availableCapacity)
  }
}

// 자동 분할 적재
const executeAutoSplit = (primaryZoneId, primaryCapacity, sameMatZones) => {
  let remainingQty = currentMaterial.value.remainingQuantity
  const newPlans = []
  const newSelectedZones = []

  // 1순위: 선택한 구역에 최대한 적재
  const primaryQty = Math.min(remainingQty, primaryCapacity)
  newPlans.push({ 
    zoneId: primaryZoneId, 
    quantity: primaryQty, 
    availableCapacity: primaryCapacity 
  })
  newSelectedZones.push(primaryZoneId)
  remainingQty -= primaryQty

  // 2순위: 같은 자재 있는 구역에 순차 적재
  for (const zoneId of sameMatZones) {
    if (remainingQty <= 0) break
    
    const available = warehouseData.value[zoneId].capacity - warehouseData.value[zoneId].occupied
    const qtyToPlace = Math.min(remainingQty, available)
    
    if (qtyToPlace > 0) {
      newPlans.push({ 
        zoneId, 
        quantity: qtyToPlace, 
        availableCapacity: available 
      })
      newSelectedZones.push(zoneId)
      remainingQty -= qtyToPlace
    }
  }

  placementPlan.value.push(...newPlans)
  selectedZones.value.push(...newSelectedZones)
  currentMaterial.value.remainingQuantity = remainingQty

  // 결과 알림
  if (remainingQty > 0) {
    toast.add({
      severity: 'warn',
      summary: '자동 분할 완료',
      detail: `남은 수량: ${remainingQty}kg\n추가 구역을 선택해주세요.`,
      life: 5000
    })
  } else {
    toast.add({
      severity: 'success',
      summary: '자동 분할 적재 완료!',
      detail: `${newPlans.length}개 구역에 분배되었습니다.`,
      life: 3000
    })
  }
}

// 수동 분할 모달 표시
const showManualSplitModal = (zoneId, availableCapacity) => {
  splitModalData.value = {
    zoneId,
    availableCapacity,
    inputQuantity: Math.min(currentMaterial.value.remainingQuantity, availableCapacity)
  }
  showSplitModal.value = true
}

// 수동 분할 적재
const executeManualSplit = () => {
  const { zoneId, inputQuantity, availableCapacity } = splitModalData.value
  
  if (inputQuantity > availableCapacity) {
    toast.add({
      severity: 'error',
      summary: '입력 오류',
      detail: '입력 수량이 가용 용량을 초과합니다!',
      life: 3000
    })
    return
  }

  if (inputQuantity > currentMaterial.value.remainingQuantity) {
    toast.add({
      severity: 'error',
      summary: '입력 오류',
      detail: '입력 수량이 남은 수량을 초과합니다!',
      life: 3000
    })
    return
  }

  const newPlan = {
    zoneId,
    quantity: inputQuantity,
    availableCapacity
  }

  placementPlan.value.push(newPlan)
  selectedZones.value.push(zoneId)
  currentMaterial.value.remainingQuantity -= inputQuantity
  
  showSplitModal.value = false
  
  toast.add({
    severity: 'success',
    summary: '수동 분할 적재 등록!',
    detail: `${zoneId}에 ${inputQuantity}kg 적재 예정`,
    life: 3000
  })
}

// 적재 실행
const executePlacement = async () => {
  if (placementPlan.value.length === 0) {
    toast.add({
      severity: 'warn',
      summary: '선택 필요',
      detail: '적재할 구역을 선택해주세요!',
      life: 3000
    })
    return
  }

  if (currentMaterial.value.remainingQuantity > 0) {
    const confirm = window.confirm(
      `⚠️ 남은 수량이 ${currentMaterial.value.remainingQuantity}kg 있습니다.\n` +
      `그래도 적재를 실행하시겠습니까?`
    )
    if (!confirm) return
  }

  try {
    // 실제 적재 처리 (API 호출 시뮬레이션)
    for (const plan of placementPlan.value) {
      // 창고 상태 업데이트
      const zone = warehouseData.value[plan.zoneId]
      zone.occupied += plan.quantity
      if (!zone.materials.includes(currentMaterial.value.materialName)) {
        zone.materials.push(currentMaterial.value.materialName)
      }
    }

    toast.add({
      severity: 'success',
      summary: '🎉 적재 완료!',
      detail: `${placementPlan.value.length}개 구역에 총 ${currentMaterial.value.totalQuantity - currentMaterial.value.remainingQuantity}kg 적재`,
      life: 5000
    })
    
    // 상태 초기화
    selectedZones.value = []
    placementPlan.value = []
    currentMaterial.value.remainingQuantity = currentMaterial.value.totalQuantity
    
  } catch (error) {
    toast.add({
      severity: 'error',
      summary: '적재 실패',
      detail: '적재 처리 중 오류가 발생했습니다!',
      life: 3000
    })
  }
}

// 현재 층의 구역들 (그리드용)
const currentFloorZones = computed(() => {
  const zones = []
  for (let row = 0; row < warehouseConfig.value.rows; row++) {
    for (let col = 1; col <= warehouseConfig.value.cols; col++) {
      const rowLabel = generateRowLabel(row)
      const zoneId = `${rowLabel}-${col}-${currentFloor.value}`
      zones.push({
        zoneId,
        rowLabel,
        col,
        row,
        ...warehouseData.value[zoneId]
      })
    }
  }
  return zones
})

// 드롭다운용 사용 가능한 구역들
const availableZonesForDropdown = computed(() => {
  return Object.keys(warehouseData.value)
    .filter(zoneId => {
      const zone = warehouseData.value[zoneId]
      return (zone.capacity - zone.occupied) > 0 && 
             zoneId.endsWith(`-${currentFloor.value}`)
    })
    .map(zoneId => {
      const zone = warehouseData.value[zoneId]
      const available = zone.capacity - zone.occupied
      const hasSame = zone.materials.includes(currentMaterial.value.materialName)
      
      return {
        label: `${zoneId} - 가용: ${available}kg ${hasSame ? '(같은자재 ✓)' : ''}`,
        value: zoneId
      }
    })
})

// 리스트뷰용 구역 데이터
const zoneListData = computed(() => {
  return Object.keys(warehouseData.value)
    .filter(zoneId => zoneId.endsWith(`-${currentFloor.value}`))
    .sort()
    .map(zoneId => {
      const zone = warehouseData.value[zoneId]
      return {
        zoneId,
        occupancyRate: getOccupancyRate(zoneId),
        availableCapacity: zone.capacity - zone.occupied,
        materials: zone.materials.join(', ') || '-',
        isSelected: selectedZones.value.includes(zoneId)
      }
    })
})

// 선택 모달에서 드롭다운 변경 처리
const handleDropdownZoneSelect = (selectedZoneId) => {
  if (selectedZoneId) {
    handleZoneClick(selectedZoneId)
  }
}

// 초기화
onMounted(() => {
  initializeWarehouse()
})
</script>

<template>
  <div class="p-6 max-w-7xl mx-auto">
    <h1 class="text-3xl font-bold mb-6">🏗️ Vue 고급 창고 적재 시스템</h1>
    
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- 왼쪽: 적재 정보 -->
      <div>
        <Card>
          <template #title>📦 적재 대상 자재</template>
          <template #content>
            <div class="space-y-3">
              <div><strong>자재명:</strong> {{ currentMaterial.materialName }}</div>
              <div><strong>LOT번호:</strong> {{ currentMaterial.lotNo }}</div>
              <div><strong>총 수량:</strong> {{ currentMaterial.totalQuantity }}{{ currentMaterial.unit }}</div>
              <div><strong>남은 수량:</strong> 
                <span :class="currentMaterial.remainingQuantity > 0 ? 'text-red-600 font-bold' : 'text-green-600 font-bold'">
                  {{ currentMaterial.remainingQuantity }}{{ currentMaterial.unit }}
                </span>
              </div>
            </div>
          </template>
        </Card>

        <!-- 적재 계획 -->
        <Card v-if="placementPlan.length > 0" class="mt-4">
          <template #title>📋 적재 계획</template>
          <template #content>
            <div class="space-y-2 max-h-40 overflow-auto">
              <div v-for="(plan, index) in placementPlan" :key="index" 
                   class="flex justify-between text-sm border-b pb-1">
                <span class="font-mono">{{ plan.zoneId }}</span>
                <span class="font-bold">{{ plan.quantity }}kg</span>
              </div>
            </div>
            <Button
              @click="executePlacement"
              class="w-full mt-4"
              severity="success"
              size="large"
            >
              🚀 적재 실행
            </Button>
          </template>
        </Card>
      </div>

      <!-- 오른쪽: 창고 뷰 -->
      <div class="lg:col-span-2">
        <Card>
          <template #title>
            <div class="flex justify-between items-center">
              <span>🏗️ 창고 레이아웃 ({{ currentFloor }}층)</span>
              <div class="flex gap-2">
                <!-- 뷰 모드 선택 -->
                <Dropdown 
                  v-model="viewMode" 
                  :options="viewModeOptions"
                  optionLabel="label"
                  optionValue="value"
                  class="w-32"
                />
                
                <!-- 층 선택 -->
                <Dropdown 
                  v-model="currentFloor"
                  :options="floorOptions"
                  optionLabel="label"
                  optionValue="value"
                  class="w-20"
                />
              </div>
            </div>
          </template>
          
          <template #content>
            <!-- 그리드 뷰 -->
            <div v-if="viewMode === 'grid'" class="overflow-auto max-h-96 border rounded-lg p-4">
              <div 
                class="grid gap-1"
                :style="`grid-template-columns: repeat(${warehouseConfig.cols}, 1fr); width: fit-content;`"
              >
                <div
                  v-for="zone in currentFloorZones"
                  :key="zone.zoneId"
                  @click="handleZoneClick(zone.zoneId)"
                  :class="`border-2 cursor-pointer text-center flex flex-col justify-center transition-all ${getCellColor(zone.zoneId)}`"
                  :style="getCellSize"
                  :title="`구역: ${zone.zoneId}\n점유율: ${getOccupancyRate(zone.zoneId)}%\n가용: ${zone.capacity - zone.occupied}kg`"
                >
                  <div class="font-bold" :style="`font-size: ${getCellSize.fontSize}`">
                    {{ zone.rowLabel }}{{ zone.col }}
                  </div>
                  <div style="font-size: 8px;">
                    {{ getOccupancyRate(zone.zoneId) }}%
                  </div>
                </div>
              </div>
            </div>

            <!-- 드롭다운 뷰 -->
            <div v-else-if="viewMode === 'dropdown'" class="space-y-4">
              <Dropdown 
                @change="handleDropdownZoneSelect"
                :options="availableZonesForDropdown"
                optionLabel="label"
                optionValue="value"
                placeholder="구역을 선택하세요..."
                class="w-full"
              />
            </div>

            <!-- 리스트 뷰 -->
            <div v-else-if="viewMode === 'list'" class="overflow-auto max-h-96">
              <DataTable :value="zoneListData" class="text-sm">
                <Column field="zoneId" header="구역" class="font-mono" />
                <Column field="occupancyRate" header="점유율">
                  <template #body="{ data }">{{ data.occupancyRate }}%</template>
                </Column>
                <Column field="availableCapacity" header="가용량">
                  <template #body="{ data }">{{ data.availableCapacity }}kg</template>
                </Column>
                <Column field="materials" header="자재" />
                <Column header="선택">
                  <template #body="{ data }">
                    <Button
                      @click="handleZoneClick(data.zoneId)"
                      :disabled="data.availableCapacity <= 0"
                      :severity="data.isSelected ? 'success' : 'primary'"
                      size="small"
                    >
                      {{ data.isSelected ? '선택됨' : '선택' }}
                    </Button>
                  </template>
                </Column>
              </DataTable>
            </div>

            <!-- 범례 -->
            <div class="flex flex-wrap gap-4 mt-4 text-xs">
              <div class="flex items-center gap-1">
                <div class="w-4 h-4 bg-green-200 border border-green-400"></div>
                <span>같은자재</span>
              </div>
              <div class="flex items-center gap-1">
                <div class="w-4 h-4 bg-blue-500 border border-blue-700"></div>
                <span>선택됨</span>
              </div>
              <div class="flex items-center gap-1">
                <div class="w-4 h-4 bg-red-200 border border-red-400"></div>
                <span>용량부족</span>
              </div>
              <div class="flex items-center gap-1">
                <div class="w-4 h-4 bg-gray-100 border border-gray-300"></div>
                <span>사용가능</span>
              </div>
            </div>
          </template>
        </Card>
      </div>
    </div>

    <!-- 수동 분할 모달 -->
    <Dialog 
      v-model:visible="showSplitModal" 
      modal 
      header="⚖️ 수동 분할 적재"
      :style="{ width: '400px' }"
    >
      <div class="space-y-3">
        <div><strong>구역:</strong> {{ splitModalData.zoneId }}</div>
        <div><strong>가용 용량:</strong> {{ splitModalData.availableCapacity }}kg</div>
        <div><strong>남은 수량:</strong> {{ currentMaterial.remainingQuantity }}kg</div>
        
        <div>
          <label class="block font-semibold mb-1">적재할 수량:</label>
          <InputNumber
            v-model="splitModalData.inputQuantity"
            :max="Math.min(splitModalData.availableCapacity, currentMaterial.remainingQuantity)"
            :min="1"
            class="w-full"
          />
        </div>
      </div>
      
      <template #footer>
        <div class="flex gap-2">
          <Button
            @click="showSplitModal = false"
            severity="secondary"
            class="flex-1"
          >
            취소
          </Button>
          <Button
            @click="executeManualSplit"
            severity="primary"
            class="flex-1"
          >
            적재 등록
          </Button>
        </div>
      </template>
    </Dialog>
  </div>
</template>

<style scoped>
.grid {
  display: grid;
}
</style>