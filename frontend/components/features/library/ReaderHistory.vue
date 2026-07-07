<template>
    <v-navigation-drawer
        :model-value="modelValue"
        location="right"
        temporary
        width="420"
        scrollable
        @update:model-value="$emit('update:modelValue', $event)"
    >
        <div class="pa-6">
            <div class="d-flex justify-space-between align-center mb-6">
                <h2>Edit History</h2>
                <v-btn icon variant="text" @click="$emit('update:modelValue', false)">
                <v-icon>mdi-close</v-icon>
                </v-btn>
            </div>

            <div class="d-flex flex-column ga-4">
                <div
                v-for="edit in edits"
                :key="edit.id"
                class="history-entry card pa-4"
                >
                    <div class="d-flex justify-space-between align-center mb-2">
                        <div class="d-flex align-center ga-2">
                            <v-chip
                                size="x-small"
                                :color="editTypeColor(edit.editType)"
                                variant="tonal"
                            >
                                {{ edit.editType }}
                            </v-chip>
                            <span class="text-body-2 font-weight-bold">@{{ edit.editor }}</span>
                        </div>
                        <span class="text-caption text-medium-emphasis">
                            {{ formatDate(edit.committedAt) }}
                        </span>
                    </div>

                    <p class="text-caption text-medium-emphasis mb-0">
                        Section {{ edit.chunkId }}
                    </p>
                </div>
            </div>
        </div>
    </v-navigation-drawer>
</template>

<script setup>
defineProps({
  modelValue: { type: Boolean, default: false },
  edits: { type: Array, default: () => [] }
})

defineEmits(['update:modelValue'])

const editTypeColor = (type) => {
  if (type === 'INSERT') return 'success'
  if (type === 'DELETE') return 'error'
  if (type === 'UPDATE') return 'primary'
  return 'grey'
}

const formatDate = (iso) => {
  if (!iso) return ''
  return new Date(iso).toLocaleString([], {
    month: 'short', day: 'numeric',
    hour: '2-digit', minute: '2-digit'
  })
}
</script>

<style scoped>
.history-entry {
  border-left: 3px solid var(--color-border);
}
</style>