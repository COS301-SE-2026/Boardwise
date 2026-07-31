<template>
  <div class="d-flex ga-3 align-center">
    <div class="flex-1-1">
      <BaseSearch
        v-model="search"
        placeholder="Search for events..."
      />
    </div>

    <BaseButton
      variant="primary"
      prepend-icon="mdi-calendar-plus"
      @click="$emit('create-event')"
    >
      Create Event
    </BaseButton>
  </div>
</template>

<script setup>
import BaseSearch from '~/components/ui/BaseSearch.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

const search = defineModel({
  type: String,
  default: ''
})

defineEmits(['create-event','search'])
</script>