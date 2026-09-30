<template>
    <v-menu
        v-model="open"
        :close-on-content-click="false"
        location="bottom end"
        :offset="8"
    >
        <template #activator="{ props: menuProps }">
            <BaseButton
                icon
                variant="text"
                class="notif-bell"
                v-bind="menuProps"
                :aria-label="bellLabel"
                data-test="notification-bell"
            >
                <v-badge
                    :model-value="unreadCount > 0"
                    :content="badgeText"
                    color="primary"
                    class="notif-badge"
                    offset-x="-2"
                    offset-y="-2"
                >
                    <v-icon :size="iconSize">mdi-bell-outline</v-icon>
                </v-badge>
            </BaseButton>
        </template>

        <v-card class="notif-panel" rounded="lg" elevation="4" data-test="notification-panel">
            <div class="notif-header">
                <h2 class="notif-title">Notifications</h2>

                <BaseButton
                    variant="text"
                    size="small"
                    :disabled="unreadCount === 0"
                    @click="markAllAsRead"
                >
                    Mark all as read
                </BaseButton>
            </div>

            <div class="notif-body">
                <!-- Signed out -->
                <BaseEmptyState
                    v-if="!signedIn"
                    size="compact"
                    title="Sign in to see notifications"
                    message="Friend requests, event RSVPs and invites show up here."
                >
                    <template #actions>
                        <BaseButton size="small" @click="goToSignIn">Sign in</BaseButton>
                    </template>
                </BaseEmptyState>

                <!-- Loading -->
                <BaseLoadingState
                    v-else-if="isLoading && !loaded"
                    size="compact"
                    message="Fetching notifications..."
                />

                <!-- Error -->
                <BaseErrorState
                    v-else-if="error && !loaded"
                    size="compact"
                    title="Couldn't load notifications"
                    :message="error"
                    retryable
                    @retry="fetchNotifications()"
                />

                <!-- Empty -->
                <BaseEmptyState
                    v-else-if="notifications.length === 0"
                    size="compact"
                    title="You're all caught up"
                    message="New activity will show up here."
                />

                <!-- List -->
                <ul v-else class="notif-list" aria-label="Notifications">
                    <li v-for="n in notifications" :key="n.id">
                        <button
                            type="button"
                            class="notif-item"
                            :class="{ 'notif-item--unread': !n.read }"
                            @click="onSelect(n)"
                        >
                        <span
                            class="notif-item__icon"
                            :style="{ '--notif-color': getNotificationMeta(n).color }"
                        >
                            <v-icon size="20" aria-hidden="true">{{ getNotificationMeta(n).icon }}</v-icon>
                        </span>

                        <span class="notif-item__content">
                            <span class="notif-item__text">
                                <span v-if="!n.read" class="sr-only">Unread: </span>{{ describeNotification(n) }}
                            </span>

                            <span class="notif-item__time">{{ timeAgo(n.createdAt) }}</span>
                        </span>

                        <span v-if="!n.read" class="notif-item__dot" aria-hidden="true" />
                        </button>
                    </li>
                </ul>
            </div>
            </v-card>
    </v-menu>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'

import BaseButton from '~/components/ui/BaseButton.vue'
import BaseEmptyState from '~/components/ui/BaseEmptyState.vue'
import BaseErrorState from '~/components/ui/BaseErrorState.vue'
import BaseLoadingState from '~/components/ui/BaseLoadingState.vue'

import {
  useNotifications,
  describeNotification,
  getNotificationMeta,
  resolveNotificationLink
} from '~/composables/useNotification'

defineProps({
  iconSize: { type: Number, default: 26 }
})

const router = useRouter()
const {
  notifications, isLoading, error, loaded, unreadCount,
  fetchNotifications, markAsRead, markAllAsRead
} = useNotifications()

const open = ref(false)
const signedIn = ref(false)
let pollTimer = null

const badgeText = computed(() => (unreadCount.value > 9 ? '9+' : unreadCount.value))
const bellLabel = computed(() =>
  unreadCount.value > 0 ? `Notifications, ${unreadCount.value} unread` : 'Notifications'
)

const checkAuth = () => {
  signedIn.value = !!localStorage.getItem('access_token')
  return signedIn.value
}

const refresh = () => {
  if (!checkAuth()) return
  fetchNotifications({ silent: loaded.value })
}

watch(open, (isOpen) => { if (isOpen) refresh() })

onMounted(() => {
  refresh()
  pollTimer = setInterval(() => {
    if (checkAuth()) fetchNotifications({ silent: true })
  }, 60_000)
})

onBeforeUnmount(() => clearInterval(pollTimer))

const onSelect = (n) => {
  markAsRead(n.id) // don't block navigation on the request
  const link = resolveNotificationLink(n)
  open.value = false
  if (link) router.push(link)
}

const goToSignIn = () => {
  open.value = false
  router.push('/auth/signin')
}

const timeAgo = (iso) => {
  const seconds = Math.max(0, Math.floor((Date.now() - new Date(iso).getTime()) / 1000))
  if (seconds < 60) return 'Just now'
  const minutes = Math.floor(seconds / 60)
  if (minutes < 60) return `${minutes}m ago`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}h ago`
  const days = Math.floor(hours / 24)
  if (days < 7) return `${days}d ago`
  return new Date(iso).toLocaleDateString()
}
</script>

<style scoped>
.notif-bell {
  overflow: visible;
}

.notif-badge .v-badge__badge {
  min-width: 20px;
  height: 20px;
  padding: 0 5px;
  font-size: 0.75rem;
  font-weight: 700;
  color: #fff;
  border: 2px solid var(--color-surface, #fff);
  box-shadow: 0 1px 3px rgb(0 0 0 / 0.3);
}

.notif-panel {
  width: min(380px, calc(100vw - 24px));
  overflow: hidden;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
}

.notif-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  border-bottom: 1px solid var(--color-border);
}

.notif-title {
  margin: 0;
  font-size: var(--fs-h3);
}

.notif-body {
  max-height: min(420px, 60vh);
  min-height: 120px;
  overflow-y: auto;
}

.notif-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.notif-list li {
  margin: 0;
  border-bottom: 1px solid var(--color-border);
}

.notif-list li:last-child {
  border-bottom: 0;
}

.notif-item {
  display: flex;
  align-items: flex-start;
  gap: var(--space-3);
  width: 100%;
  padding: var(--space-3) var(--space-4);
  border: 0;
  background: transparent;
  color: var(--color-text);
  font: inherit;
  text-align: left;
  cursor: pointer;
  transition: background-color var(--transition-fast);
}

.notif-item:hover,
.notif-item--unread:hover {
  background: var(--color-surface-alt);
}

.notif-item:active,
.notif-item--unread:active {
  background: color-mix(in srgb, var(--color-primary) 12%, var(--color-surface));
}

.notif-item--unread {
  background: color-mix(in srgb, var(--color-primary) 6%, var(--color-surface));
}

.notif-item:focus-visible {
  outline: 3px solid var(--color-primary);
  outline-offset: -3px;
}

.notif-item__icon {
  display: grid;
  place-items: center;
  flex: 0 0 40px;
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  background: color-mix(in srgb, var(--notif-color) 12%, var(--color-surface));
  color: var(--notif-color);
}

.notif-item__content {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
  min-width: 0;
}

.notif-item__text {
  font-size: var(--fs-body);
  line-height: 1.4;
  overflow-wrap: anywhere;
}

.notif-item--unread .notif-item__text {
  font-weight: var(--fw-bold);
}

.notif-item__time {
  color: var(--color-text-muted);
  font-size: var(--fs-small);
}

.notif-item__dot {
  flex: 0 0 10px;
  width: 10px;
  height: 10px;
  margin-top: 6px;
  border-radius: 50%;
  background: var(--color-primary);
}
</style>