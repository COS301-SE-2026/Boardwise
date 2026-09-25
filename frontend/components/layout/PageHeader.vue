<template>
  <div class="d-flex flex-column ga-5" :datat-test="dataTest">
    <div class="page-header__top">
      <SectionTitle
        :title="title"
        :subtitle="subtitle"
      />

      <BaseButton
        v-if="actionLabel"
        variant="primary"
        :block="mobile"
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
const { mobile } = useDisplay()

import SectionTitle from '../ui/SectionTitle.vue'
import BaseButton from '../ui/BaseButton.vue'

defineProps({
  title: { type: String, required: true },
  subtitle: { type: String, default: '' },
  actionLabel: { type: String, default: '' },
  actionIcon: { type: String, default: 'mdi-plus' },
  dataTest: { type: String, default: 'page-header' }
})
</script>