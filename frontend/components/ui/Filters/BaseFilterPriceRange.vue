<template>
    <div class="filter-price-range">
        <v-text-field
            :model-value="min"
            placeholder="Min"
            prefix="R"
            type="number"
            density="compact"
            hide-details
            @update:model-value="$emit('update:min', $event)"
        />
        <v-text-field
            :model-value="max"
            placeholder="Max"
            prefix="R"
            type="number"
            density="compact"
            hide-details
            @update:model-value="$emit('update:max', $event)"
        />
    </div>
</template>

<script setup>
defineProps({
  min: { type: [String, Number], default: '' },
  max: { type: [String, Number], default: '' }
})
defineEmits(['update:min', 'update:max'])
</script>