<template>
  <div class="d-flex flex-wrap ga-4">
    <BaseButton prepend-icon="mdi-book-open-variant" @click="navigateTo('/library/${rulebook.id}/read')">
      Read Rulebook
    </BaseButton>

    <BaseButton variant="secondary" prepend-icon="mdi-store" @click="navigateTo('/marketplace')">
      Browse Marketplace
    </BaseButton>
  </div>
</template>

<script setup>
import BaseButton from '~/components/ui/BaseButton.vue'
import {useRouter} from 'vue-router'

const router = useRouter()

const props = defineProps({
  rulebook: {
    type: Object,
    required: true
  }
})
</script>