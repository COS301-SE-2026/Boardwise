import { ref } from 'vue'
import { friendService, type FriendSummary, type FriendRequestSummary, type FriendStatus } from '~/services/friendService'
import { useSnackBar } from './useSnackbar'

const { show } = useSnackBar()

export const useFriends = () => {
    const friends = ref<FriendSummary[]>([])
    const mutuals = ref<FriendSummary[]>([])
    const pendingRequests = ref<FriendRequestSummary[]>([])
    const loading = ref(false)
    const error = ref('')

    const fetchFriends = async (username: string) => {
        loading.value = true
        error.value = ''

        try {
            const res = await friendService.getFriends(username)
            friends.value = res.filter(f => !f.isMutual)
            mutuals.value = res.filter(f => f.isMutual)
        } catch (err: any) {
            error.value = err.data?.message || 'Failed to load friends'
            show(error.value, 'error')
        } finally {
            loading.value = false
        }
    }

    const fetchPendingRequests = async () => {
        try {
            pendingRequests.value = await friendService.getPendingRequests()
        } catch (err: any) {
            error.value = err.data?.message || 'Failed to load friend requests'
        }
    }

    const sendRequest = async (username: string) => {
        try {
            await friendService.sendFriendRequest(username)
            show('Friend request sent', 'success')
        } catch (err: any) {
            show(err.data?.message || 'Failed to send request', 'error')
            throw err
        }
    }

    const respondToRequest = async (requestId: string, action: 'accept' | 'reject') => {
        try {
            await friendService.respondToRequest(requestId, action)
            pendingRequests.value = pendingRequests.value.filter(r => r.requestId !== requestId)
            show(action == 'accept' ? 'Friend request accepted' : 'Friend request declined', 'success')
        } catch (err: any) {
            show(err.data?.message || 'Failed to send request', 'error')
            throw err
        }
    }

    const removeFriend = async (username: string) => {
         try {
            await friendService.removeFriend(username)
            friends.value = friends.value.filter(f => f.username !== username)
            mutuals.value = mutuals.value.filter(f => f.username !== username)
            show('Friend removed', 'info')
        } catch (err: any) {
            show(err.data?.message || 'Failed to remove friend', 'error')
            throw err
        }
    }

    return {
        friends, mutuals, pendingRequests, loading, error, 
        fetchFriends, fetchPendingRequests, sendRequest, respondToRequest, removeFriend
    }
}