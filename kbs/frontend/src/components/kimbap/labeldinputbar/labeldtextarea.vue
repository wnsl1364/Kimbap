<template>
  <div class="flex flex-col gap-1">
    <label class="text-[16px] font-bold text-gray-800 leading-none mb-2">
      {{ label }}
    </label>
    <textarea
      ref="textareaRef"
      :placeholder="placeholder"
      :rows="rows"
      :cols="cols"
      :disabled="disabled"
      :readonly="readonly"
      :value="modelValue"
      @input="onInput"
      class="border border-gray-300 rounded-xl px-3 py-2 text-[14px] text-gray-800 bg-white resize-none 
         focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-blue-400"
     />
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'

defineEmits(['update:modelValue'])

const props = defineProps({
  label: { type: String, required: true },
  modelValue: { type: String, default: '' },
  placeholder: { type: String, default: '' },
  rows: { type: Number, default: 3 },
  cols: { type: Number, default: 30 },
  autoResize: { type: Boolean, default: true },
  readonly: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false },
})

const textareaRef = ref(null)

function resize() {
  const el = textareaRef.value
  if (props.autoResize && el) {
    el.style.height = 'auto'
    el.style.height = el.scrollHeight + 'px'
  }
}

function onInput(e) {
  const value = e.target.value
  emit('update:modelValue', value)
  resize()
}

onMounted(() => {
  resize()
})

watch(() => props.modelValue, () => {
  resize()
})
</script>
