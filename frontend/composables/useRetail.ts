import type { returnValue } from 'happy-dom/lib/PropertySymbol'
import { ref } from 'vue'

export interface RetailResult {
    retailerName: string
    retailTitle: string
    price?: number
    imageUrl?: string
    url?: string
}

ecport const useRetail = () => {
    const retailResults = ref<RetailResult[]>([])
    const retailLoading = ref(false)
    const retailError = ref<string | null>(null)

    const fetchRetail = async (query: string) => {
        if(!query?.trim()) {
            retailResults.value = []
            return
        }

        retailLoading.value = true
        retailError.value = null

        try {
            const config = useRuntimeConfig()
            
            const data = await $fetch<RetailResult[]>(`${config.public.apiBase}/marketplace/retail`, 
                { 
                    query: {
                        query
                    }
                }
            )

            retailResults.value = data ?? []
        }
        catch (error: any) {
            console.error('Failed to fetch retail results', error)
        }
    }

    const clearRetail = () => {
        retailResults.value = []
        retailError.value = null
    }

    return {
        retailResults,
        retailLoading,
        retailError,
        fetchRetail,
        clearRetail
    }
}



