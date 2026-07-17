<template> 
    <div class="d-flex flex-column ga-6">

        <SectionTitle 
        title="Communities" 
        subtitle="Join a community to connect with others"
        />

        <div class="d-flex flex-wrap ga-4 align-center">
            <BaseSearch 
              v-model="searchQuery" 
              placeholder="Find a community..."
              />

            <BaseTag  
              v-model="activeTab"
              :tabs="tabs"
              />
        </div>  

        <CommunityGrid 
          :communities="filteredCommunities"
          @join-request="$emit('join-request', $event)"
        />

    </div>
</template>

<script setup>
import { ref, computed  } from 'vue';

import SectionTitle from '~/components/ui/SectionTitle.vue';
import BaseSearch from '~/components/ui/BaseSearch.vue';
import BaseTag from '~/components/ui/BaseTag.vue';
import CommunityGrid from './CommunityGrid.vue';

const props = defineProps({
    communities: {
      type: Array,
      defualt: () => []
    }
})

defineEmits(['join-request'])

const searchQuery = ref('')
const activeTab = ref('All')

const tabs = [
  'All',
  'My Group',
  'Popular'
]

const filteredCommunities = computed(() => {
  return communities.value.filter((community) => {
    return community.name
      .toLowerCase()
      .includes(searchQuery.value.toLowerCase())
  })
})
</script>
