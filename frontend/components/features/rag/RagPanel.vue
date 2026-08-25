<template>
    <v-navigation-drawer
        :model-value="modelValue"
        location="right"
        temporary
        :width="panelWidth"
        @update:model-value="$emit('update:modelValue', $event)"
    >
        <div class="rag-panel">
            <div class="rag-header d-flex justify-space-between align-center pa-4">
                <h3 class="text-h6 mb-0">{{  rulebook?.title  }} - Ask AI</h3>
                <v-btn icon="mdi-close" variant="text" size="small" @click="close" />

                <RagFeed :messages="messages" :is-loading="isLoading" />
                <RagComposer :is-loading="isLoading" @send="handleSend" />
            </div> 
        </div>
    </v-navigation-drawer>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue'
import { useDisplay } from 'vuetify'

import RagFeed from './RagComposer.vue'
import RagComposer from './RagComposer.vue'

import { useRag } from '~/composables/useRag.ts'

const props = defineProps<{
    modelValue: boolean
    rulebook: { id: string, title: string } | null
}> ()

const emit = defineEmits<{
    (e: 'update:modelValue', value: boolean): void
}> ()

const { lgAndUp } = useDisplay()
const panelWidth = computed(() => lgAndUp.value ? 420 : '100%')

const { messages, isLoading, askQuestion, clearConversation } = useRag()

const close = () => emit('update:modelValue', false)

const handleSend = (query: string) => {
    if (!props.rulebook?.id) return
    askQuestion(props.rulebook.id, query)
}


watch(() => props.rulebook?.id, () => clearConversation())
</script>

<style scoped>
.rag-panel {
    display: flex;
    flex-driection: column;
    height: 100%;
}
</style>