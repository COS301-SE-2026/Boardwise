<template> 
    <div class="gcommunity-view">
        <SectionTitle title="Communities" subtitle="Join a community to connect with others"/>

        <div class="community-view__actions">
            <BaseSearch v-model="searchQuery" placeholder="Find a community..."/>
            <BaseTag :tabs="['All', 'My groups', 'Trending ']" v-model="activeTab"/>
        </div>  

        <CommunityGrid 
        :communities="filteredCommunities"
        @join-request="handleJoin"/>

    </div>
</template>

<script setup>
import SectionTitle from '~/components/ui/SectionTitle.vue';
import BaseSearch from '~/components/ui/BaseSearch.vue';
import BaseTag from '~/components/ui/BaseTag.vue';
import CommunityGrid from './CommunityGrid.vue';
import { ref, computed  } from 'vue';

const searchQuery = ref('')
const activeTab = ref('All')
 
const communities = ref([
    { id: 1, name: 'Board Game Lovers', category: 'General', memberCount: 120, 
    description: 'A community for board game enthusiasts to share their love for all things board games.' },
    { id: 2, name: 'Strategy Gamers', category: 'Strategy', memberCount: 450, description: 
    'A community for fans of strategy games to discuss tactics, share game recommendations, and connect with like-minded players.' },
    { id: 3, name: 'Family Game Night', category: 'Family', memberCount: 100, description: 
    'A community for families to play games together and build stronger relationships.' }
])

const filteredCommunities = computed(() => {
  return communities.value.filter((community) => {
    return community.name
      .toLowerCase()
      .includes(searchQuery.value.toLowerCase())
  })
})

const handleJoin = (id) => {
  console.log(`Join request for community ID: ${id}`)
}
</script>
