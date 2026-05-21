<template>
    <v-expansion-panels 
      :model-value="isOpen ? [0] : []"
      variant="accordion"
      flat
      @update:model-value="isOpen = $event.length > 0" 
    >
      <v-expansion-panel :title="title">
        <v-expansion-panel-text>
          <slot />
        </v-expansion-panel-text>
      </v-expansion-panel>
    </v-expansion-panels>
</template>

<script setup>

const props = defineProps({
    title: String, 
    defaultOpen: {
        type: Boolean,
        default: true
    }
})

const isOpen = ref(props.defaultOpen)
</script>