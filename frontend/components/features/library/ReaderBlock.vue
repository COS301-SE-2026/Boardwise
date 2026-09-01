<template>
    <v-card
        elevation="0"
        class="block-wrapper pa-4 mb-2"
        :class="{'bg-grey-lighten-4': isEditing}"
        rounded="lg"
    >
    <!-- Read view -->
        <div v-if="!isEditing" class="text-body-1 text-medium-emphasis" style="line-height: 1.9;" v-html="parsedContent"></div>

    <!-- Edit View -->
        <div v-else class="d-flex flex-column ga-3">
            <BaseTextArea
            v-model="draftContent"
            placeholder="Edit section content..."
            :rows="5"
            :auto-grow="false"
            density="comfortable"
            hide-details
            maxlength="1000"
            counter="1000"
            bg-color="surface"
            :disabled="isSaving"
            />
                <!-- Action Bar -->
            <div class="d-flex justify-space-between align-center flex-wrap ga-2 mt-1">
                <div class="d-flex ga-2">
                    <BaseButton
                        size="small"
                        variant="text"
                        color="error"
                        prepend-icon="mdi-delete"
                        :disabled="isSaving"
                        @click="$emit('delete', chunk.chunkId)"
                    >
                        Delete
                    </BaseButton>
                    <BaseButton
                        size="small"
                        variant="text"
                        color="primary"
                        prepend-icon="mdi-plus"
                        :disabled="isSaving"
                        @click="$emit('insert-below', index + 1)"
                    >
                        Add Below
                    </BaseButton>
                </div>

                <div class="d-flex ga-2">
                    <BaseButton
                        size="small"
                        variant="ghost"
                        :disabled="isSaving"
                        @click="handleCancel"
                    >
                        Cancel
                    </BaseButton>
                    <BaseButton
                        size="small"
                        prepend-icon="mdi-content-save"
                        :loading="isSaving"
                        :disabled="!isDirty"
                        @click="handleSave"
                    >
                        Save
                    </BaseButton>
                </div>
            </div>
        </div>
    </v-card>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import {marked} from 'marked'
import DOMPurify from 'dompurify'
import BaseButton from '~/components/ui/BaseButton.vue'
import BaseTextArea from '~/components/ui/BaseTextArea.vue'

const props = defineProps({
    chunk: {type: Object, required: true},
    index: {type: Number, required: true},
    isEditing: { type: Boolean, default: false },
    isSaving: { type: Boolean, default: false },
    searchQuery: { type: String, default: ''},
    activeOccurrence: { type: Number, default: -1 }
})

const emit = defineEmits(['save', 'cancel', 'insert-below', 'delete'])

marked.use({
    gfm: true,
    breaks: true
})

const draftContent = ref(props.chunk?.content ?? '')

const isDirty = computed(() => draftContent.value !== (props.chunk?.content ?? ''))

watch(() => props.chunk?.content, (newContent) => {
    draftContent.value = newContent ?? ''
})

const handleSave = () => {
    if(isDirty.value){
        emit('save', {chunkId: props.chunk.chunkId, content: draftContent.value })
    }
}

const handleCancel = () => {
    draftContent.value = props.chunk?.content ?? ''
    emit('cancel')
}

const parsedContent = computed(() => {
    const text = props.chunk?.content ?? ''

    const rawHtml = marked.parse(text)

    let cleanHtml = DOMPurify.sanitize(rawHtml, {
        ALLOWED_TAGS: [
            'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'p', 'ul', 'ol', 'li',
            'strong', 'em', 'table', 'thead', 'tbody', 'tr', 'th', 'td',
            'br', 'blockquote', 'a', 'img', 'mark'
        ],
        ALLOWED_ATTR: ['href', 'title', 'target', 'src', 'alt', 'class']
    })

    if(props.searchQuery.trim()){
        const escaped = props.searchQuery.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
        const regex = new RegExp(`(?![^<]*>)(${escaped})`, 'gi')
        let matchCount = 0

        cleanHtml = cleanHtml.replace(regex, (match) => {
            const isActive = matchCount === props.activeOccurrence
            matchCount++
            return isActive
            ? `<mark class="search-highlight search-highlight--active">${match}</mark>`
            : `<mark class="search-highlight">${match}</mark>`
        })
    }

    return cleanHtml
})
</script>

<style scoped>
.search-highlight {
  background: #fff176;
  border-radius: 2px;
  padding: 0 2px;
}

.search-highlight--active {
  background: #ffb300;
  outline: 2px solid #e65100;
}
</style>