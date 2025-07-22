<script setup>
const props = defineProps({
    data: Object,
    fields: Array
})

const emit = defineEmits(['update:data'])

const updateField = (field, value) => {
    emit('update:data', { ...props.data, [field]: value })
}
</script>

<template>
  <div class="bg-white rounded shadow p-6">
    <div class="grid grid-cols-2 gap-x-8 gap-y-4">
      <div v-for="(field, index) in fields" :key="field.field" class="flex items-center space-x-2">
        <label class="w-28 text-left font-bold">{{ field.label }}</label>
        <div class="flex-1 relative">
          <template v-if="!field.suffixButton">
            <input
              :value="data[field.field]"
              @input="updateField(field.field, $event.target.value)"
              :readonly="field.readonly"
              :type="field.type === 'date' ? 'date' : 'text'"
              class="border rounded p-1 w-full h-10"
            />
          </template>
          <template v-else>
            <div class="flex">
              <input
                :value="data[field.field]"
                @input="updateField(field.field, $event.target.value)"
                :readonly="field.readonly"
                type="text"
                class="border rounded-l p-1 w-full h-10"
              />
              <button type="button"
                class="rounded px-2 ml-1 bg-blue-600 hover:bg-blue-700 whitespace-nowrap h-10 text-white">
                {{ field.suffixButton }}
            </button>
            </div>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

