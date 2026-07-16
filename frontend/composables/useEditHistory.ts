import { ref } from 'vue'

export const useEditHistory = () => {
    const editHistory = ref<any[]>([])
    const isLoadingHistory = ref(false)

    const fetchEditHistory = async (rulebookId: string) => {
        isLoadingHistory.value = true

        try {
            // TODO: replace with real API call when backend endpoint is ready
            // const { $api } = useNuxtApp();
            // const data = await $api(`vault/rulebooks/${rulebookId}/history`);
            // editHistory.value = data.edits;

            // Mock data — remove when backend is ready
            editHistory.value = [
                {
                    id: '1',
                    editor: 'jane_dev',
                    editType: 'UPDATE',
                    chunkId: 'Section 1',
                    previousContent: 'Roll the dice and move your token.',
                    newContent: 'Roll two six-sided dice and move your token clockwise.',
                    committedAt: new Date(Date.now() - 1000 * 60 * 30).toISOString()
                },
                {
                    id: '2',
                    editor: 'bob_rules',
                    editType: 'INSERT',
                    chunkId: 'Section 2',
                    previousContent: null,
                    newContent: 'Rolling doubles grants an additional turn.',
                    committedAt: new Date(Date.now() - 1000 * 60 * 10).toISOString()
                },
                {
                    id: '3',
                    editor: 'jane_dev',
                    editType: 'DELETE',
                    chunkId: 'Section 3',
                    previousContent: 'This section has been removed.',
                    newContent: null,
                    committedAt: new Date(Date.now() - 1000 * 60 * 2).toISOString()
                }
            ]
        } catch (err) {
            console.error(`Failed to fetch edit history for ${rulebookId}:`, err)
            editHistory.value = []
        } finally {
            isLoadingHistory.value = false;
        }
    }

    return {
        editHistory,
        isLoadingHistory,
        fetchEditHistory
    }
}