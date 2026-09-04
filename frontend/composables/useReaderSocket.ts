import { onUnmounted } from 'vue';
import { useStomp } from '~/composables/useStomp';

interface LockAcquiredEvent{
    rulebookId: string;
    lockedByUserId: string;
    lockedByUsername: string;
    expiresAt: string;
    currentVersion: number;
}

interface LockReleasedEvent{
    rulebookId: string;
    releasedByUserId: string;
    releasedByUsername: string;
    reason: 'voluntary' | 'expired' | 'disconnected';
    releasedAt: string;
}

interface DeltaCommittedEvent{
    eventType: 'DELTA_COMMITTED';
    rulebookId: string;
    editorId: string;
    version: number;
    timestamp: string;
    chunkId: string;
    deltaContent: string;
}

interface ChunkInsertedEvent{
    eventType: 'CHUNK_INSERTED';
    rulebookId: string;
    editorId: string;
    version: number;
    timestamp: string;
    chunkId: string;
    content: string;
    index: number;
}

interface ChunkDeletedEvent{
    eventType: 'CHUNK_DELETED';
    rulebookId: string;
    editorId: string;
    version: number;
    timestamp: string;
    chunkId: string;
}

interface SocketHandlers{
    onLockAcquired: (payload: LockAcquiredEvent) => void;
    onLockReleased: (payload: LockReleasedEvent) => void;
    onDeltaCommitted: (payload: DeltaCommittedEvent) => void;
    onChunkInserted: (payload: ChunkInsertedEvent) => void;
    onChunkDeleted: (payload: ChunkDeletedEvent) => void;
    onReconnect: () => void;
}

export const useReaderSocket = (rulebookId: string, handlers: SocketHandlers) => {
    const { isConnected, subscribe, unsubscribe } = useStomp();

    const rulebookDests = {
        lockAcquired: `/topic/vault/rulebooks/${rulebookId}/lock/acquired`,
        lockReleased: `/topic/vault/rulebooks/${rulebookId}/lock/released`,
        deltaCommited: `/topic/vault/rulebooks/${rulebookId}/delta`,
        chunkInserted: `/topic/vault/rulebooks/${rulebookId}/chunk/inserted`,
        chunkDeleted: `/topic/vault/rulebooks/${rulebookId}/chunk/deleted`
    };

    const subToAll = () => {
        subscribe(rulebookDests.lockAcquired, handlers.onLockAcquired);
        subscribe(rulebookDests.lockReleased, handlers.onLockReleased);
        subscribe(rulebookDests.deltaCommited, handlers.onDeltaCommitted);
        subscribe(rulebookDests.chunkInserted, handlers.onChunkInserted);
        subscribe(rulebookDests.chunkDeleted, handlers.onChunkDeleted);
    };

    const unSubToAll = () => {
        unsubscribe(rulebookDests.lockAcquired);
        unsubscribe(rulebookDests.lockReleased);
        unsubscribe(rulebookDests.deltaCommited);
        unsubscribe(rulebookDests.chunkInserted);
        unsubscribe(rulebookDests.chunkDeleted);
    };

    if(isConnected.value)
        subToAll();
    else{
        const stop = watch(isConnected, (connected) => {
            if(connected){
                subToAll();
                stop();
            }
        })
    }
    
    onUnmounted(() => unSubToAll());
    return { isConnected };
}