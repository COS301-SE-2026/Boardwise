<template>
  <div class="d-flex flex-column ga-5" :data-test="dataTest">
    <div class="page-header__top d-flex flex-column flex-md-row justify-md-space-between align-md-center ga-4">
      <SectionTitle
        :title="title"
        :subtitle="subtitle"
      />

      <BaseButton
        v-if="actionLabel"
        variant="primary"
        :block="smAndDown"
        :prepend-icon="actionIcon"
        @click="$emit('action')"
      >
        {{ actionLabel }}
      </BaseButton>
    </div>

    <slot name="search" />
  </div>
</template>

<script setup>
import { useDisplay } from 'vuetify'

import SectionTitle from '../ui/SectionTitle.vue'
import BaseButton from '../ui/BaseButton.vue'

const { smAndDown } = useDisplay()

defineProps({
  title: { type: String, required: true },
  subtitle: { type: String, default: '' },
  actionLabel: { type: String, default: '' },
  actionIcon: { type: String, default: 'mdi-plus' },
  dataTest: { type: String, default: 'page-header' }
})

defineEmits(['action'])
</script>