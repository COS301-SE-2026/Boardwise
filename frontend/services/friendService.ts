export interface FriendSummary {
    id: string
    username: string
    profilePicture: string | null
    isMutual: boolean
}

export interface FriendRequestSummary {
    requestId: string
    fromUsername: string
    fromProfilePicture: string | null
}

export type FriendStatus = 'none' | 'pendingSent' | 'pendingReceived' | 'friends'

// Expected functionality. No service contract given by backend
export const friendService = {
    getFriends(username: string) {
        const { $api } = useNuxtApp()
        return $api<FriendSummary[]>(`users/${username}/friends`)
    },

    getPendingRequests() {
        const { $api } = useNuxtApp()
        return $api<FriendRequestSummary[]>(`users/friend-request`)
    },

    sendFriendRequest(username: string) {
        const { $api } = useNuxtApp()
        return $api<{message: string }>(`users/${username}/friend-request`, { method: 'POST'})
    },

    respondToRequest(requestId: string, action: 'accept' | 'reject'){
        const { $api } = useNuxtApp()
        return $api<{ message: string }>(`users/friend-request/${requestId}`, { 
            method: 'PATCH',
            body: { action }
        })
    },

    removeFriend(username: string) {
        const { $api } = useNuxtApp()
        return $api<{ message: string }>(`users/${username}/friend`, { method: 'DELETE'})
    }
}