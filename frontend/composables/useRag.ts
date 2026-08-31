import { ref } from 'vue'
import { ragService, type Citation } from '~/services/ragService'
import { useSnackBar } from './useSnackbar'

const { show } = useSnackBar()

export interface RagMessage {
    id: string
    role: 'user' | 'assistant'
    content: string
    citations?: Citation[]
    isError?: boolean
    query?: string
}

export const useRag = () => {
    const messages = ref<RagMessage[]>([])
    const isLoading = ref(false)
    const error = ref('')

    const askQuestion = async (rulebookId: string, query: string) => {
        const trimmed = query.trim()
        if(!trimmed) return 

        messages.value.push({
            id: crypto.randomUUID(),
            role: 'user',
            content: trimmed,
            query: trimmed
        })

        isLoading.value = true
        error.value = ''

        try {
            const res = await ragService.queryRulebook(rulebookId, trimmed)
            messages.value.push({
                id: crypto.randomUUID(),
                role: 'assistant',
                content: res.answer,
                citations: res.citations
            })
        } catch (err: any) {
            const message = err.data?.message || 'Failed to get an answer. Please try again.'
            error.value = message
            messages.value.push({
                id: crypto.randomUUID(),
                role: 'assistant',
                content: message,
                isError: true,
                query: trimmed
            })

            show(message, 'error')
        } finally {
            isLoading.value = false
        }
    }

    const clearConversation = () => {
        messages.value = []
        error.value = ''
    }

    return { messages, isLoading, error, askQuestion, clearConversation }
}