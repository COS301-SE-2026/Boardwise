<template>
    <div class="d-flex flex-column ga-1">
        <div 
            v-for="opt in normalizedOptions" 
            :key="opt.value" 
            class="filter-checkbox-row"
        >
            <v-checkbox
                :label="opt.label"
                :model-value="isChecked(opt.value)"
                density="compact"
                color="primary"
                hide-details
                @update:model-value="toggle(opt.value)"
            />

            <span 
                v-if="opt.count !== undefined" 
                class="filter-checkbox-row__count"
            >
                {{ opt.count }}
            </span>
        </div>
    </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  options: { type: Array, required: true }, 
  modelValue: { type: Array, default: () => [] }
})

const emit = defineEmits(['update:modelValue'])

const normalizedOptions = computed(() =>
  props.options.map(opt => typeof opt === 'string' ? { label: opt, value: opt } : opt)
)

const isChecked = (value) => props.modelValue.includes(value)

const toggle = (value) => {
  const next = isChecked(value)
    ? props.modelValue.filter(v => v !== value)
    : [...props.modelValue, value]
    
  emit('update:modelValue', next)
}
</script>