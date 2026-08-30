import { ref, computed } from 'vue'
import { RetailService, type RetailListings, type PageImplRetailPage } from '~/services/retailService'

// Re-export so components can keep importing RetailResult from useRetail
export type RetailResult = RetailListings

export const useRetail = () => {
    const retailResults = ref<RetailResult[]>([])
    const retailLoading = ref(false)
    const retailError = ref<string | null>(null)

    // Pagination
    const persPage = ref(0)
    const totalElements = ref(0)
    const isLastRetailPage = ref(false)
    const hasMoreRetail = computed(() => !isLastRetailPage.value)



    const fetchPersonalisedListings = async (reset = false) => {
        if (reset) {
            persPage.value = 0
            retailResults.value = []
            isLastRetailPage.value = false
        }

        // Don't request beyond the last page
        if (isLastRetailPage.value) {
            return
        }

        retailLoading.value = true
        retailError.value = null
        const { show } = useSnackBar()

        try {
            const res: PageImplRetailPage = await RetailService.getPersonalisedListings(persPage.value)

            retailResults.value = reset
                ? (res?.content ?? [])
                : [...retailResults.value, ...(res?.content ?? [])]

            totalElements.value = res?.totalElements ?? 0
            isLastRetailPage.value = res?.last ?? true
            persPage.value = (res?.number ?? persPage.value) + 1

            return res
        }
        catch (err: any) {
            retailError.value = err
            console.error(err)
            show("couldn't fetch retail listings", "error")
        }
        finally {
            retailLoading.value = false
        }
    }

    const clearRetail = () => {
        retailResults.value = []
        retailError.value = null
        persPage.value = 0
        totalElements.value = 0
        isLastRetailPage.value = false
    }

    return {
        retailResults,
        retailLoading,
        retailError,
        persPage,
        totalElements,
        hasMoreRetail,
        clearRetail,
        fetchPersonalisedListings
    }
}