<template>
 <PageContainer>
    <div class="community-detail" v-if="community">
  
      <CommunityBanner 
        :community="community"
        :is-joined="isJoined"
        @toggle-join="toggleJoin"
        @edit="openEditModal"
      />

      <CommunityTabs 
        :active-tab="activeTab" 
        @change="handleTabChange"
      />

      <div class="tab-content">
        <template v-if="activeTab === 'Collection'">
          <CollectionGrid 
            :games="games"
            @add-game="handleAddGame"
          />
        </template>

        <template v-else-if="activeTab === 'Events'">
          <CommunityEvents 
            :community-id="community.id"
            :events="filteredEvents"
            @create-event="handleCreateEvent"
            @join-event="handleJoinEvent"
          />
        </template>

        <template v-else-if="activeTab === 'Members'">
          <MemberList 
            :community="community"
            @invite="handleInvite"
          />
        </template>

        <template v-else-if="activeTab === 'Discussion'">
          <CommunityChats 
            :community="community"
            :messages="messages"
            @send="handleSendMessage"
          />
        </template>

        <template v-else-if="activeTab === 'About'">
          <CommunityAbout :community="community" />
        </template>
      </div>

      <CommunityEditModal
        v-model="showEditModal"
        :community="community"
        @save="handleSave"
      />
    </div>

    <div v-else class="loading-state">
      <v-progress-circular indeterminate color="primary" />
    </div>
  </PageContainer>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import PageContainer from '~/components/layout/PageContainer.vue'
import CommunityBanner from '~/components/features/community/shared/CommunityBanner.vue'
import CommunityTabs from '~/components/features/community/shared/CommunityTabs.vue'
import CollectionGrid from '~/components/features/community/shared/CollectionGrid.vue'
import CommunityEvents from '~/components/features/community/shared/CommunityEvents.vue'
import MemberList from '~/components/features/community/members/MemberList.vue'
import CommunityChats from '~/components/features/community/shared/CommunityChats.vue'
import CommunityAbout from '~/components/features/community/shared/CommunityAbout.vue'
import CommunityEditModal from '~/components/features/community/models/CommunityEditModal.vue'
import { communities as mockCommunities } from '~/mockdata/communities.js'
import { games as mockGames } from '~/mockdata/games.js'
import { events as mockEvents } from '~/mockdata/events.js'


const route = useRoute()
const communityId = computed(() => Number(route.params.id))

const community = ref(null)
const isJoined = ref(false)
const activeTab = ref('Collection')
const showEditModal = ref(false)

const games = ref(mockGames)

const allEvents = ref(mockEvents)
const filteredEvents = computed(() => {
  if (!community.value) return []
  return allEvents.value.filter(e => e.communityId === community.value.id)
})

const messages = ref([
  {
    id: 1,
    name: 'Thabo M.',
    avatar: '/images/avatar.jpg',
    text: 'Hey everyone! Anyone up for a game this weekend?',
    time: '10:12',
    isOwn: false
  },
  {
    id: 2,
    name: 'You',
    avatar: '/images/avatar.jpg',
    text: "I am! Let's do Catan.",
    time: '10:15',
    isOwn: true
  }
])

onMounted(() => {
  const found = mockCommunities.find(c => c.id === communityId.value)
  if (found) {
    community.value = { ...found }
    // Check if user is a member
    isJoined.value = found.members_list?.some(m => m.name === 'You' || m.role === 'Member') || false
  }
})

const toggleJoin = () => {
  isJoined.value = !isJoined.value
  community.value.members += isJoined.value ? 1 : -1
  
  if (isJoined.value) {
    if (!community.value.members_list) community.value.members_list = []
    community.value.members_list.push({
      id: Date.now(),
      name: 'You',
      role: 'Member',
      avatar: '/images/avatar.jpg'
    })
  } else {
    community.value.members_list = community.value.members_list.filter(m => m.name !== 'You')
  }
}

const handleTabChange = (tab) => {
  activeTab.value = tab
}

const handleAddGame = () => {
  console.log('Add game to collection')
}

const handleCreateEvent = () => {
  console.log('Create event')
}

const handleJoinEvent = (eventId) => {
  const event = allEvents.value.find(e => e.id === eventId)
  if (event) {
    event.rsvped = !event.rsvped
    if (event.rsvped) {
      event.attendees.push('You')
    } else {
      event.attendees = event.attendees.filter(a => a !== 'You')
    }
  }
}

const handleSendMessage = (text) => {
  messages.value.push({
    id: Date.now(),
    name: 'You',
    avatar: '/images/avatar.jpg',
    text,
    time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    isOwn: true
  })
}

const handleInvite = (invite) => {
  console.log('Invite:', invite)
}

const openEditModal = () => {
  showEditModal.value = true
}

const handleSave = (data) => {
  community.value.name = data.name
  community.value.description = data.description
  community.value.type = data.type
  community.value.image = data.image
  showEditModal.value = false
}
</script>

<style scoped>
.community-detail {
  padding-bottom: var(--space-8);
}

.tab-content {
  margin-top: var(--space-6);
}

.loading-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}
</style>