import { ref } from 'vue'
import { LibraryService } from '~/services/libraryService'
import type { FetchError } from 'ofetch'

const isEditing = ref<boolean>(false)
const isSaving = ref<boolean>(false)
const lockHeldBy = ref<string|null>(null)
const lockExpiresAt = ref<string|null>(null)
const lockError = ref<string>('')
const canUndo = ref<boolean>(false)
const canRedo = ref<boolean>(false)
const currentVersion = ref<number>(0);

export const useEditLock = () => {
    const acquireLock = async (rulebookId: string) => {
        try{
            const response = await LibraryService.acquireWriteLock(rulebookId);
            if(response.lockGranted){
                lockHeldBy.value = response.lockedBy;
                lockExpiresAt.value = response.expiresAt;
                currentVersion.value = response.currentVersion;
            }
            return response.lockGranted;
        }catch(err){
            const fetchError = err as FetchError<{message: string}>;

            lockError.value = fetchError.data?.message || 'Failed to acquire write lock';

            return false;
        }
    }

    const releaseLock = async (rulebookId: string) => {
        try{
            await LibraryService.releaseWriteLock(rulebookId);
            lockHeldBy.value = null;
            lockExpiresAt.value = null;
            isEditing.value = false;
            canUndo.value = false;
            canRedo.value = false;
        }catch(err){
            const fetchError = err as FetchError<{message: string}>;
            lockError.value = fetchError.data?.message || 'Failed to release write lock';
        }
    }

    const releaseAllLocks = async () => {
        try{
            await LibraryService.releaseAllWriteLocks();
            lockHeldBy.value = null;
            lockExpiresAt.value = null;
            isEditing.value = false;
            canUndo.value = false;
            canRedo.value = false;
        }catch(err){
            const fetchError = err as FetchError<{message: string}>;
            lockError.value = fetchError.data?.message || 'Failed to release all write locks';
        }
    }

    const commitDelta = async (rulebookId: string, chunkId: string, content: string, expectedVersion: number) => {
        try{
            const response = await LibraryService.commitEditDelta(rulebookId, {
                "expectedVersion":expectedVersion,
                "content": content,
                "chunkId": chunkId
            })
            if(response.done){
                lockExpiresAt.value = new Date(response.lockExpiresAt).toISOString();
                canUndo.value = true;
                canRedo.value = false;
                currentVersion.value = response.newVersion;
                return response.newVersion;
            }
            return expectedVersion;
        }catch(err){
            const fetchError = err as FetchError<{message: string}>;
            lockError.value = fetchError.data?.message || 'Failed to commit the edit';
            throw err;
        }
    }

    const undoEdit = async (rulebookId: string, expectedVersion: number) => {
        try{
            const response = await LibraryService.undoEdit(rulebookId, {
                "expectedVersion":expectedVersion,
            });
            if(response.done){
                canRedo.value = true;
                lockExpiresAt.value = new Date(response.lockExpiresAt).toISOString();
                currentVersion.value = response.newVersion;
                return response.newVersion;
            }
            return expectedVersion;
        }catch(err){
            const fetchError = err as FetchError<{message: string}>;
            if(fetchError.data?.message.includes("No action to undo")){
                canUndo.value = false;
            }
            lockError.value = fetchError.data?.message || 'Failed to undo the edit';
            throw err;
        }
    }

    const redoEdit = async (rulebookId: string, expectedVersion: number) => {
        try{
            const response = await LibraryService.redoEdit(rulebookId, {
                "expectedVersion":expectedVersion
            });
            if(response.done){
                canUndo.value = true;
                lockExpiresAt.value = new Date(response.lockExpiresAt).toISOString();
                currentVersion.value = response.newVersion;
                return response.newVersion;
            }
            return expectedVersion;
        }catch(err){
            const fetchError = err as FetchError<{message: string}>;
            if(fetchError.data?.message.includes("No action to redo")){
                canRedo.value = false;
            }
            lockError.value = fetchError.data?.message || 'Failed to redo the edit';
            throw err;
        }
    }

    const startEditing = async (rulebookId: string) => {
        lockError.value = ''
        const granted = await acquireLock(rulebookId)
        if (granted) {
            isEditing.value = true;
            canUndo.value = false;
            canRedo.value = false;
        }
    }

    const stopEditing = async (rulebookId: string) => {
        await releaseLock(rulebookId)
        isEditing.value = false;
        isSaving.value = false;
    }

    const insertChunk = async (rulebookId: string, content: string, chunkBeforeId: string | null, expectedVersion: number, insertIndex: number) => {
        try{
            const response = await LibraryService.insertChunk(rulebookId, {
                expectedVersion: expectedVersion,
                content: content,
                chunkBeforeId: chunkBeforeId,
                insertIndex: insertIndex
            });
            if(response.done){
                lockExpiresAt.value = new Date(response.lockExpiresAt).toISOString();
                canUndo.value = true;
                canRedo.value = false;
                currentVersion.value = response.newVersion;
                return { newVersion: response.newVersion, newChunkId: response.chunkId };
            }
            throw new Error("Failed to insert");
        }catch(err){
            const fetchError = err as FetchError<{message: string}>;
            lockError.value = fetchError.data?.message || 'Failed to insert the chunk';
            throw err;
        }
    }

    const deleteChunk = async (rulebookId: string, chunkId: string, chunkBeforeId: string| null, expectedVersion: number) => {
        try{
            const response = await LibraryService.deleteChunk(rulebookId, {
                expectedVersion: expectedVersion,
                chunkId: chunkId,
                chunkBeforeId: chunkBeforeId
            });
            if(response.done){
                lockExpiresAt.value = new Date(response.lockExpiresAt).toISOString();
                canUndo.value = true;
                canRedo.value = false;
                currentVersion.value = response.newVersion;
                return response.newVersion;
            }
            throw new Error("Failed to delete");
        }catch(err){
            const fetchError = err as FetchError<{message: string}>;
            lockError.value = fetchError.data?.message || 'Failed to delete the chunk';
            throw err;
        }
    }

    return {
        isEditing,
        isSaving,
        lockHeldBy,
        lockExpiresAt,
        lockError,
        canUndo,
        canRedo,
        currentVersion,
        startEditing,
        stopEditing,
        commitDelta,
        releaseAllLocks,
        undoEdit,
        redoEdit,
        insertChunk,
        deleteChunk
    }
}