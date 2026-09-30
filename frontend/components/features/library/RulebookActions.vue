<template>
  <div class="d-flex flex-column ga-2">
    <BaseButton block prepend-icon="mdi-book-open-variant"  :to="`/library/read/${rulebook.id}`">
      Read Rulebook
    </BaseButton>

    <BaseButton block variant="secondary" prepend-icon="mdi-store" to="/marketplace">
      Browse Marketplace
    </BaseButton>

    <BaseButton block variant="secondary" prepend-icon="mdi-wizard-hat" :disabled="!rulebook.id" :to="`/setup-wizard/${rulebook.id}`">
      Setup Wizard
    </BaseButton>
  </div>
</template>

<script setup>
import BaseButton from '~/components/ui/BaseButton.vue'

const props = defineProps({
  rulebook: {
    type: Object,
    required: true
  }
})
</script>