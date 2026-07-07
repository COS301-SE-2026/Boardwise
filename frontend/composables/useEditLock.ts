import { ref } from 'vue'

export const useEditLock = () => {
    const isEditing = ref(false)
    const isSaving = ref(false)
    const lockHeldBy = ref<string|null>(null)
    const lockExpiresAt = ref<string|null>(null)
    const lockError = ref<string>('')

    const acquireLock = async (rulebookId: string) => {
       // TODO: replace with real API call
        // const response = await LibraryService.acquireLock(rulebookId)
        // if (!response.data.lockGranted) {
        //   lockHeldBy.value = response.data.lockedBy
        //   lockExpiresAt.value = response.data.expiresAt
        //   lockError.value = `Lock held by @${response.data.lockedBy}`
        //   return false
        // }
        // lockExpiresAt.value = response.data.expiresAt
        // return true

        // Mock — always grants lock
        lockHeldBy.value   = null
        lockExpiresAt.value = new Date(Date.now() + 5 * 60 * 1000).toISOString()
        return true
    }

    const releaseLock = async (rulebookId: string) => {
        // TODO: replace with real API call
        // await LibraryService.releaseLock(rulebookId)

        // Mock — just clears state
        lockHeldBy.value    = null
        lockExpiresAt.value = null
    }

     const commitDelta = async (rulebookId: string, chunkId: string, deltaContent: string, expectedVersion: number) => {
        // TODO: replace with real API call
        // const response = await LibraryService.commitDelta(rulebookId, {
        //   expectedVersion,
        //   chunkId,
        //   deltaContent
        // })
        // return response.data.newVersion

        // Mock — just returns a bumped version
        return expectedVersion + 1
    }

    const startEditing = async (rulebookId: string) => {
        lockError.value = ''
        const granted = await acquireLock(rulebookId)
        if (granted) isEditing.value = true
    }

    const stopEditing = async (rulebookId: string) => {
        await releaseLock(rulebookId)
        isEditing.value = false
        isSaving.value  = false
    }

    return {
        isEditing,
        isSaving,
        lockHeldBy,
        lockExpiresAt,
        lockError,
        startEditing,
        stopEditing,
        commitDelta
    }
}