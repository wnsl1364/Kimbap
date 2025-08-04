<script setup>
import { ref, watch } from 'vue'
import SingleSelectModal from '@/components/kimbap/modal/singleselect.vue'

const props = defineProps({
  data: Object,
  fields: {
    type: Array,
    default: () => []
  },
  title: {
    type: String,
    default: ''
  },
  buttons: {
    type: Object,
    default: () => ({
      save: { show: true, label: '저장', severity: 'success' },
      reset: { show: true, label: '초기화', severity: 'secondary' },
      delete: { show: false, label: '삭제', severity: 'danger' },
      load: { show: false, label: '불러오기', severity: 'info' }
    })
  },
  buttonPosition: {
    type: String,
    default: 'top',
    validator: (value) => ['top', 'bottom', 'both'].includes(value)
  },
  modalDataSets: {
    type: Object,
    default: () => ({})
  },
  dataKey: {
    type: String,
    required: true
  }
})

const emit = defineEmits([
  'update:data',
  'dataChange',
  'openQtyModal',
  'delete',
  'reset',
  'save',
  'load'
])

const updateField = (field, value) => {
  emit('update:data', { ...props.data, [field]: value })
  emit('dataChange', { ...props.data, [field]: value })
}

// 모달 관련 상태
const modalVisible = ref(false)
const currentField = ref('')
const currentRowData = ref(null)

const openModal = (rowData, field) => {
  if (props.modalDataSets[field]) {
    currentRowData.value = rowData
    currentField.value = field
    modalVisible.value = true
  } else {
    alert(`${field} 필드의 모달 데이터가 없습니다.`)
  }
}

const handleModalSelect = (selectedItem) => {
  if (currentRowData.value && currentField.value && selectedItem) {
    const modalConfig = props.modalDataSets[currentField.value]

    if (modalConfig?.mappingFields) {
      for (const [targetField, sourceField] of Object.entries(modalConfig.mappingFields)) {
        updateField(targetField, selectedItem[sourceField])
      }
    } else {
      const displayValue = selectedItem[modalConfig.displayField]
      updateField(currentField.value, displayValue)
    }

    emit('select', selectedItem)

    if (modalConfig?.emitEvent) {
      emit(modalConfig.emitEvent, selectedItem)
    }
    if (typeof modalConfig?.onSelect === 'function') {
      modalConfig.onSelect(selectedItem);
    }
  }
  resetModalState()
}

const handleModalClose = (visible) => {
  modalVisible.value = visible
  if (!visible) resetModalState()
}

const resetModalState = () => {
  modalVisible.value = false
  currentRowData.value = null
  currentField.value = ''
}

const handleButtonClick = (type) => {
  // 상단 버튼 클릭 시 모달 열기 가능하게
  if (props.modalDataSets[type]) {
    currentRowData.value = props.data
    currentField.value = type
    modalVisible.value = true
  } else {
    emit(type)
  }
}
</script>

<template>
  <div class="bg-white rounded shadow p-6">
    <div class="flex justify-between items-center mb-4">
      <h2 class="text-lg mb-0 font-semibold">{{ title }}</h2>
      <div v-if="buttonPosition === 'top' || buttonPosition === 'both'" class="flex justify-end gap-2">
        <slot name="top-buttons"></slot>
        <Button v-if="buttons.delete?.show" :label="buttons.delete.label" :severity="buttons.delete.severity" @click="$emit('delete')" />
        <Button v-if="buttons.reset?.show" :label="buttons.reset.label" :severity="buttons.reset.severity" @click="$emit('reset')" />
        <Button v-if="buttons.save?.show" :label="buttons.save.label" :severity="buttons.save.severity" @click="$emit('save')" />
        <Button v-if="buttons.load?.show" :label="buttons.load.label" :severity="buttons.load.severity" @click="handleButtonClick('load')" />
      </div>
    </div>

    <div class="grid grid-cols-2 gap-x-8 gap-y-4">
      <div v-for="(field, index) in fields" :key="field.field" class="flex items-center space-x-2">
        <label class="w-28 text-left font-bold">{{ field.label }}</label>
        <div class="flex-1 relative">
          <template v-if="field.type === 'calendar'">
            <Calendar v-model="props.data[field.field]"
                      dateFormat="yy-mm-dd"
                      :minDate="field.minDate"
                      :maxDate="field.maxDate"
                      showIcon class="w-full" />
          </template>

          <template v-else>
            <div class="flex items-center w-full h-10">
              <input
                :value="props.data[field.field]"
                @input="updateField(field.field, $event.target.value)"
                :type="field.inputType || 'text'"
                :readonly="field.readonly"
                :disabled="field.disabled"
                class="border rounded flex-1 px-3 py-2"
              />
              <i
                v-if="field.suffixIcon"
                :class="[field.suffixIcon, 'cursor-pointer text-gray-400 px-3 py-2 hover:text-blue-500']"
                @click.stop="openModal(props.data, field.field)"
              />
              <button v-if="field.suffixButton"
                      type="button"
                      class="rounded px-2 ml-2 bg-blue-600 hover:bg-blue-700 whitespace-nowrap h-10 text-white"
                      @click="openModal(props.data, field.field)">
                {{ field.suffixButton }}
              </button>
            </div>
          </template>
        </div>
      </div>
    </div>
  </div>

  <!-- 모달 연결 -->
  <SingleSelectModal
    v-model:visible="modalVisible"
    :key="`${currentField}-${Date.now()}`"
    :modelValue="null"
    @update:modelValue="handleModalSelect"
    @update:visible="handleModalClose"
    :items="props.modalDataSets[currentField]?.items || []"
    :columns="props.modalDataSets[currentField]?.columns || []"
    :itemKey="props.dataKey"
  />
</template>
