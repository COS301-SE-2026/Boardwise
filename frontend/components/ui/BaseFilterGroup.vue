<template>
    <v-expansion-panels 
      v-model="openPanels"
      variant="accordion"
      flat
    >
      <v-expansion-panel>
        <v-expansion-panel-title class="text-body-2 font-weight-bold">
          {{ title }}
        </v-expansion-panel-title>
        <v-expansion-panel-text>
          <slot />
        </v-expansion-panel-text>
      </v-expansion-panel>
    </v-expansion-panels>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
    title: String, 
    defaultOpen: {
        type: Boolean,
        default: true
    }
})

const openPanels = ref(props.defaultOpen ? [0] : [])
</script>