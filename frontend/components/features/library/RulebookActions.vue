<template>
  <div class="d-flex flex-wrap ga-4">
    <BaseButton prepend-icon="mdi-book-open-variant" @click="goToReader">
      Read Rulebook
    </BaseButton>

    <BaseButton variant="secondary" prepend-icon="mdi-store" @click="goToMarketplace">
      Browse Marketplace
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

const goToMarketplace = () => {
  navigateTo('/marketplace00')
}

const goToReader = () => {
  navigateTo('library/${props.rulebook.id}/read')
}
</script>