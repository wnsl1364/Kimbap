<template>
  <div class="flex flex-col gap-1">
    <label class="text-[16px] font-bold text-gray-800 tracking-tight leading-none mb-2">
      {{ label }}
    </label>
    <input
    :type="type"
    :placeholder="placeholder"
    :disabled="disabled"
    :readonly="readonly"
    :value="modelValue"
    @input="$emit('update:modelValue', $event.target.value)"
    class="border border-gray-300 rounded-xl px-3 py-2 text-[14px] text-gray-800 bg-white resize-none 
         focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-blue-400"
    />
  </div>
</template>



<script setup>

defineEmits(['update:modelValue'])

defineProps({
  label: { type: String, required: true },
  modelValue: { type: String, default: '' },
  placeholder: { type: String, default: '' },
  readonly: { type: Boolean, default: false }, 
  disabled: { type: Boolean, default: false },
  type: { type: String, default: 'text' },
})

</script>