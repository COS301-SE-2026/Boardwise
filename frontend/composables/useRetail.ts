import { ref, computed } from 'vue'
import { createSharedComposable } from '@vueuse/core'
import { RetailService, type RetailListings, type PageImplRetailPage } from '~/services/retailService'
import { useSnackBar } from './useSnackbar'

export type RetailResult = RetailListings

const RETAIL_CACHE_TTL_MS = 5 * 60 * 1000

const _useRetail = () => {
    const { show } = useSnackBar()

    const retailResults = ref<RetailResult[]>([])
    const retailLoading = ref(false)
    const retailError = ref<string | null>(null)

    // Pagination
    const persPage = ref(0)
    const totalElements = ref(0)
    const isLastRetailPage = ref(false)
    const hasMoreRetail = computed(() => !isLastRetailPage.value)

    // Cache bookkeeping
    const lastFetchedAt = ref(0)
    const hasFetched = ref(false)

    const isCacheFresh = () =>
        hasFetched.value &&
        Date.now() - lastFetchedAt.value < RETAIL_CACHE_TTL_MS

    const invalidateRetailCache = () => {
        lastFetchedAt.value = 0
        hasFetched.value = false
    }

    const fetchPersonalisedListings = async (reset = false) => {
        if (reset) {
            // Already have fresh results from a recent fetch — skip the network call.
            if (isCacheFresh()) {
                return
            }
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

        try {
            const res: PageImplRetailPage = await RetailService.getPersonalisedListings(persPage.value)

            retailResults.value = reset
                ? (res?.content ?? [])
                : [...retailResults.value, ...(res?.content ?? [])]
            totalElements.value = res?.totalElements ?? 0
            isLastRetailPage.value = res?.last ?? true
            persPage.value = (res?.number ?? persPage.value) + 1
            lastFetchedAt.value = Date.now()
            hasFetched.value = true
            return res
        }
        catch (err: any) {
            retailError.value = err?.message ?? 'Failed to fetch retail listings'
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
        invalidateRetailCache()
    }

    return {
        retailResults,
        retailLoading,
        retailError,
        persPage,
        totalElements,
        hasMoreRetail,
        clearRetail,
        fetchPersonalisedListings,
        invalidateRetailCache
    }
}

export const useRetail = createSharedComposable(_useRetail)