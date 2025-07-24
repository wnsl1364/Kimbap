<script setup>
const props = defineProps({
    data: Object,
    fields: {
        type: Array,
        default: () => []
    },
    title: {
        type: String,
        default: ''
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
}
</script>

<template>
  <div class="bg-white rounded shadow p-6">
    <div class="flex justify-between items-center mb-4">
        <h2 class="text-lg mb-0 font-semibold">{{ title }}</h2>
        
        <div v-if="buttonPosition === 'top' || buttonPosition === 'both'" class="flex justify-end gap-2">
            <!-- 슬롯 버튼들 -->
            <slot name="top-buttons"></slot>
            
            <!-- 기본 버튼들 -->
            <Button v-if="buttons.delete?.show" :label="buttons.delete.label" :severity="buttons.delete.severity" @click="$emit('delete')"/>
            <Button v-if="buttons.reset?.show" :label="buttons.reset.label" :severity="buttons.reset.severity" @click="$emit('reset')" />
            <Button v-if="buttons.save?.show" :label="buttons.save.label" :severity="buttons.save.severity" @click="$emit('save')"/>
            <Button v-if="buttons.load?.show" :label="buttons.load.label" :severity="buttons.load.severity" @click="$emit('load')"/>
        </div>
    </div>
    <div class="grid grid-cols-2 gap-x-8 gap-y-4">
      <div v-for="(field, index) in fields" :key="field.field" class="flex items-center space-x-2">
        <label class="w-28 text-left font-bold">{{ field.label }}</label>
        <div class="flex-1 relative">
          <template v-if="field.type === 'calendar'">
              <!-- <Calendar v-model="data[field.field]" dateFormat="yy-mm-dd" showIcon class="w-full" /> -->
              <Calendar :modelValue="data[field.field]"
                @update:modelValue="updateField(field.field, $event)" dateFormat="yy-mm-dd" :minDate="field.minDate" :maxDate="field.maxDate" showIcon class="w-full"/>
          </template>

          <template v-else-if="field.type === 'input'">
              <div class="flex items-center border rounded w-full h-10">
                  <input
                      v-model="data[field.field]"
                      :type="field.inputType || 'text'"
                      :readonly="field.readonly"
                      :disabled="field.disabled"
                      class="border-none outline-none flex-1 bg-transparent px-3 py-2"
                  />
                  <i
                      v-if="field.suffixIcon"
                      :class="[field.suffixIcon, 'cursor-pointer text-gray-400 px-3 py-2']"
                      @click="$emit(field.suffixEvent, data)"
                  />
              </div>
          </template>

          <template v-else>
              <div class="flex">
                  <input
                      :value="data[field.field]"
                      @input="updateField(field.field, $event.target.value)"
                      :readonly="field.readonly"
                      :disabled="field.disabled"
                      :type="field.type === 'date' ? 'date' : 'text'"
                      class="border rounded-l p-1 w-full h-10"
                  />
                  <button v-if="field.suffixButton"
                      type="button"
                      class="rounded px-2 ml-1 bg-blue-600 hover:bg-blue-700 whitespace-nowrap h-10 text-white"
                      @click="$emit(field.suffixEvent, data)">
                      {{ field.suffixButton }}
                  </button>
              </div>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

