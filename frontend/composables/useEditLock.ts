import { ref } from 'vue'
import { LibraryService } from '~/services/libraryService'

export const useEditLock = () => {
    const isEditing = ref(false)
    const isSaving = ref(false)
    const lockHeldBy = ref<string|null>(null)
    const lockExpiresAt = ref<string|null>(null)
    const lockError = ref<string>('')
    const canUndo = ref(false)
    const canRedo = ref(false)

    const acquireLock = async (rulebookId: string) => {
       // TODO: replace with real API call

       // Possible code 

        // try {
        //     const data = await LibraryService.acquireLock(rulebookId)
        //     if (!data.lockGranted) {
        //         lockHeldBy.value    = data.lockedBy
        //         lockExpiresAt.value = data.expiresAt
        //         lockError.value     = `Lock held by @${data.lockedBy}`
        //         return false
        //     }
        //     lockHeldBy.value    = null
        //     lockExpiresAt.value = data.expiresAt
        //     return true
        //     } catch (err: any) {
        //     if (err?.status === 409) {
        //         lockError.value = err.data?.message || 'Rulebook is currently being edited'
        //     } else {
        //         lockError.value = 'Failed to acquire lock'
        //     }
        //     return false
        //     }
        // }

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

        // Possible code:
        // TODO: Review possibly code

        // try {
        //     await LibraryService.releaseLock(rulebookId)
        // } catch {
        // // Silent fail — lock will expire naturally
        // } finally {
        //     lockHeldBy.value    = null
        //     lockExpiresAt.value = null
        //     canUndo.value       = false
        //     canRedo.value       = false
        // }

        // Mock — just clears state
        lockHeldBy.value    = null
        lockExpiresAt.value = null
    }

    const releaseAllLocks = async (rulebookId: string) => {
        try{
            await LibraryService.releaseAllLocks(rulebookId)
        } catch {
            // Silent fail
        }
    }

     const commitDelta = async (rulebookId: string, chunkId: string, deltaContent: string, expectedVersion: number) => {
        // TODO: replace with real API call
        // const response = await LibraryService.commitDelta(rulebookId, {
        //   expectedVersion,
        //   chunkId,
        //   deltaContent
        // })
        // return response.data.newVersion

        // Possible code: 
        // TODO: Review code

        // const data = await LibraryService.commitDelta(rulebookId, {
        //     chunkId,
        //     deltaContent,
        //     expectedVersion
        // })
        // lockExpiresAt.value = new Date(Date.now() + 5 * 60 * 1000).toISOString()
        // canUndo.value = true
        // canRedo.value = false
        // return data.newVersion

        // Mock — just returns a bumped version
        return expectedVersion + 1
    }

    const undoEdit = async (rulebookId: string, expectedVersion: number) => {
        try {
            const data = await LibraryService.undoEdit(rulebookId, { expectedVersion })
            lockExpiresAt.value = new Date(Date.now() + 5 * 60 * 1000).toISOString()
            return data.newVersion
        } catch (err: any) {
            if(err?.status === 409 ) canUndo.value = false
            throw err
        }
    }

    const redoEdit = async (rulebookId: string, expectedVersion: number) => {
        try {
            const data = await LibraryService.redoEdit(rulebookId, { expectedVersion })
            lockExpiresAt.value = new Date(Date.now() + 5 * 60 * 1000).toISOString()
            return data.newVersion
        } catch (err: any) {
            if(err?.status === 409 ) canRedo.value = false
            throw err
        }
    }

    const startEditing = async (rulebookId: string) => {
        lockError.value = ''
        const granted = await acquireLock(rulebookId)
        if (granted) {
            isEditing.value = true
            canUndo.value = false
            canRedo.value = false
        }
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
        canUndo,
        canRedo,
        startEditing,
        stopEditing,
        commitDelta,
        releaseAllLocks,
        undoEdit,
        redoEdit
    }
}