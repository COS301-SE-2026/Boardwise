import { ref } from 'vue'
import { LibraryService } from '~/services/libraryService'
import type { FetchError } from 'ofetch'

const editHistory = ref<any[]>([]);
const isLoadingHistory = ref<boolean>(false);
const historyError = ref<string>('');

export const useEditHistory = () => {
    const fetchEditHistory = async (rulebookId: string) => {
        isLoadingHistory.value = true
        historyError.value = '';

        try {
            const response = await LibraryService.fetchEditHistory(rulebookId);

            if(response && response.edits){
                editHistory.value = response.edits.reverse(); // for newest edits to appear at the top of the array.
            }else{
                editHistory.value = [];
            }
        } catch (err) {
            const fetchError = err as FetchError<{message: string}>;
            historyError.value = fetchError.data?.message || 'Failed to load edit history.';
            editHistory.value = [];
            console.error(`Failed to fetch edit history for ${rulebookId}:`, err)
        } finally {
            isLoadingHistory.value = false;
        }
    }

    return {
        editHistory,
        isLoadingHistory,
        historyError,
        fetchEditHistory
    }
}