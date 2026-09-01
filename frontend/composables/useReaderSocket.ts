import { ref, onUnmounted } from 'vue';
import { Client, type IMessage } from '@stomp/stompjs';

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
    const isConnected = ref<boolean>(false);
    const hasConnectedOnce = ref<boolean>(false);

    let stompClient: Client | null = null;

    const connect = () => {
        try {
            const token = import.meta.client ? localStorage.getItem('access_token') : null;
            const brokerURL = useRuntimeConfig().public.wsBaseUrl;
            
            stompClient = new Client({
                brokerURL,
                connectHeaders:{
                    Authorization: `Bearer ${token}`
                },
                reconnectDelay: 5000,
                heartbeatIncoming: 4000,
                heartbeatOutgoing: 4000,
            });

            stompClient.onConnect = (frame: any) => {
                if(!stompClient) return;

                isConnected.value = true;

                if(hasConnectedOnce.value){
                    handlers.onReconnect();
                }else{
                    hasConnectedOnce.value = true;
                }

                // Subscribe to Lock Acquisition events
                stompClient.subscribe(`/topic/vault/rulebooks/${rulebookId}/lock/acquired`, (message: IMessage) => {
                    const payload: LockAcquiredEvent = JSON.parse(message.body);
                    handlers.onLockAcquired(payload);
                });

                // Subscribe to Lock Release events
                stompClient.subscribe(`/topic/vault/rulebooks/${rulebookId}/lock/released`, (message: IMessage) => {
                    const payload: LockReleasedEvent = JSON.parse(message.body);
                    handlers.onLockReleased(payload);
                });

                // Subscribe to Delta Commit events
                stompClient.subscribe(`/topic/vault/rulebooks/${rulebookId}/delta`, (message: IMessage) => {
                    const payload: DeltaCommittedEvent = JSON.parse(message.body);
                    handlers.onDeltaCommitted(payload);
                });
                // Subscribe to Chunk Insertion events
                stompClient.subscribe(`/topic/vault/rulebooks/${rulebookId}/chunk/inserted`, (message: IMessage) => {
                    const payload: ChunkInsertedEvent = JSON.parse(message.body);
                    handlers.onChunkInserted(payload);
                });
                // Subscribe to Chunk Deletion events
                stompClient.subscribe(`/topic/vault/rulebooks/${rulebookId}/chunk/deleted`, (message: IMessage) => {
                    const payload: ChunkDeletedEvent = JSON.parse(message.body);
                    handlers.onChunkDeleted(payload);
                });
            }

            stompClient.onStompError = (frame: any) => {
                console.error('Broker reported error: ' + frame.headers['message']);
                console.error('Additional details: ' + frame.body);
            }

            stompClient.activate();
        } catch (err) {
            console.error('Failed to connect to WebSocket:', err);
        }
    };

    const disconnect = () => {
        if (stompClient?.active) {
            stompClient.deactivate();
            isConnected.value = false;
        }
    };

    onUnmounted(() => disconnect());

    return { isConnected, connect, disconnect };
}