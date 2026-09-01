import { ref, watch } from 'vue'
import { useDebounceFn } from '@vueuse/core'

interface UseDebouncedAutocompleteOptions {
    debounceMs?: number
    fetchOnMount?: boolean
}

export function useDebouncedAutocomplete(
    fetchOptions: (query: string) => Promise<any[]>,
    opts: UseDebouncedAutocompleteOptions = {}
) {
    const { debounceMs = 300, fetchOnMount = true } = opts

    const search = ref('')
    const options = ref<any[]>([])
    const isSelecting = ref(false)

    const runFetch = useDebounceFn(async (query) => {
        if (query === null || query === undefined) return
        try {
            options.value = await fetchOptions(query)
        } catch (err) {
            console.error('failed to load options: ', err)
        }
    }, debounceMs)

    watch(search, (val) => {
        if (isSelecting.value) {
            isSelecting.value = false
            return
        }
        if (val !== null && val !== undefined) {
            runFetch(val)
        }
    })

    const markSelecting = () => {
        isSelecting.value = true
    }

    if (fetchOnMount) {
        runFetch('')
    }

    return { search, options, markSelecting, refetch: runFetch }
}