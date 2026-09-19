<template>
  <v-menu v-bind="menuProps">
    <template #activator="{ props: actProps }">
      <BaseButton 
        v-bind="{ ...actProps, ...$attrs }" 
        :variant="buttonVariant"
        :aria-label="ariaLabel || label"
      >
        <slot name="activator">
          {{ label }}
          <v-icon end aria-hidden="true">mdi-chevron-down</v-icon>
        </slot>
      </BaseButton>
    </template>

    <v-list rounded="lg" elevation="2" v-bind="listProps">
      <slot />
    </v-list>
  </v-menu>
</template>

<script setup>
import BaseButton from './BaseButton.vue';

defineOptions({
  inheritAttrs: false
})

defineProps({
  label: {
    type: String,
    default: 'Options'
  },
  ariaLabel: {
    type: String,
    default: ''
  },
  menuProps: {
    type: Object,
    default: () => ({})
  },
  listProps: {
    type: Object,
    default: () => ({})
  }
})

const attributes = useAttrs()
const buttonVariant = computed(() => attributes.variant ?? 'secondary')
</script>