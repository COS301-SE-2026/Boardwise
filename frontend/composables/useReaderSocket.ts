import { ref, onUnmounted } from 'vue';

export const useReaderSocket = (
    rulebookId: string,
    onLockAcquired: (data: { lockedBy: string; expiresAt: string }) => void,
    onLockReleased: () => void
) => {
    const isConnected = ref(false)
    let socket: WebSocket | null = null

    const connect = () => {
        try {
            const wsUrl = useRuntimeConfig().public.wsUrl ?? 'ws://localhost:8080/ws'
            const token = localStorage.getItem('access_token')
            const url = `${wsUrl}/vault/rulebooks/${rulebookId}/lock${token ? `?token=${token}` : ''}`
            
            socket = new WebSocket(url)

            socket.onopen = () => {
                isConnected.value = true;
            }

            socket.onmessage = (event) => {
                try {
                    const data = JSON.parse(event.data);

                    if (data.type === 'LOCK_ACQUIRED') {
                        onLockAcquired({ lockedBy: data.lockedBy, expiresAt: data.expiresAt })
                    } else if (data.type === 'LOCK_RELEASED') {
                        onLockReleased()
                    }
                } catch {
                    console.error('Failed to parse WebSocket message:', event.data);
                }
            }

            socket.onclose = () => {
                isConnected.value = false;
            }

            socket.onerror = (err) => {
                console.error('WebSocket error:', err)
            }

        } catch (err) {
            console.error('Failed to connect to WebSocket:', err);
        }
    };

    const disconnect = () => {
        if (socket) {
            socket.close();
            socket = null;
            isConnected.value = false;
        }
    };

    onUnmounted(() => disconnect());

    return { isConnected, connect, disconnect };
}