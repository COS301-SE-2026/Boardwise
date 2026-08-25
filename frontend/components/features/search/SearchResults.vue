<template> 
    <div>
        <SectionTitle :title="`Search results for “${query}”.`" />

        <BaseTabs 
            :tabs="tabLabels"
            :active-tab="activeTab"
            @change="activeTab = $event"
        />

        <div class="results-body">
            <template v-if="activeTab === 'All' || activeTab === 'People'">
                <SectionHeader
                    title="People"
                    :show-see-all="activeTab === 'All'"
                    @see-all="activeTab = 'People'"
                />

                <BaseEmptyState
                    v-if="!people.length"
                    title="No people found"
                    :message="`No accounts matched “${query}”.`"
                />

                <BaseGrid v-else cols="200px">
                    <PersonCard
                        v-for="person in visible(people)"
                        :key="person.id"
                        :person="person"
                        @click="$emit('open-profile', person)"
                        @friend-action="$emit('friend-action', person)"
                    />
                </BaseGrid>
            </template>

            <template v-if="activeTab === 'All' || activeTab === 'Rulebooks'">
                <SectionHeader
                    title="Rulebooks"
                    :show-see-all="activeTab === 'All'"
                    @see-all="activeTab = 'Rulebooks'"
                />

                <BaseEmptyState
                    v-if="!rulebooks.length"
                    title="No rulebooks found"
                    :message="`No rulebooks matched “${query}”.`"
                />

                <BaseGrid v-else cols="200px">
                    <RulebookCard
                        v-for="rb in visible(rulebooks)"
                        :key="rb.id"
                        :rulebook="rb"
                        @click="$emit('open-rulebook', rb)"
                    />
                </BaseGrid>            
            </template>

            <template v-if="activeTab === 'All' || activeTab === 'Listings'">
                <SectionHeader
                    title="Listings"
                    :show-see-all="activeTab === 'All'"
                    @see-all="activeTab = 'Listings'"
                />

                <BaseEmptyState
                    v-if="!listings.length"
                    title="No listings found"
                    :message="`No marketplace listings matched “${query}”.`"
                />

                <BaseGrid v-else cols="200px">
                    <ListingCard
                        v-for="listing in visible(listings)"
                        :key="listing.id"
                        :listing="listing"
                        @click="$emit('open-listing', listing)"
                    />
                </BaseGrid>
            </template>

            <template v-if="activeTab === 'All' || activeTab === 'Communities'">
                <SectionHeader 
                    title="Communities"
                    :show-see-all="activeTab === 'All'"
                    @see-all="activeTab = 'Communities'"
                />

                <BaseEmptyState
                    v-if="!communities.length"
                    title="No communities found"
                    :message="`No communities matched “${query}”.`"
                />

                <BaseGrid v-else cols="200px">
                    <CommunityCard
                        v-for="community in visible(communities)"
                        :key="community.id"
                        :community="community"
                    />
                </BaseGrid>
            </template>
        </div>
    </div>
</template>

<script setup>
import { ref } from 'vue'

import SectionTitle from '~/components/ui/SectionTitle.vue';
import BaseEmptyState from '~/components/ui/BaseEmptyState.vue';
import BaseGrid from '~/components/ui/BaseGrid.vue';
import BaseTabs from '~/components/ui/BaseTabs.vue';

import SectionHeader from './SectionHeader.vue';
import PersonCard from '~/components/features/people/PersonCard.vue';
import RulebookCard from '~/components/features/library/RulebookCard.vue';
import ListingCard from '~/components/features/marketplace/ListingCard.vue';
import CommunityCard from '~/components/features/community/CommunityCard.vue';

defineProps({
    query: { type: String, default: ''},
    people: { type: Array, default: () => [] },
    rulebooks: { type: Array, default: () => [] },
    listings: { type: Array, default: () => [] },
    communities: { type: Array, default: () => [] }
})

defineEmits(['friend-action','open-profile', 'open-rulebook', 'open-listing'])

const tabLabels = [ 'All', 'People', 'Rulebooks', 'Listings', 'Communities']
const activeTab = ref('All')

const PREVIEW_LIMIT = 4
const visible = (list) => activeTab.value === 'All' ? list.slice(0, PREVIEW_LIMIT) : list
</script>

<style scoped>
.results-body {
    padding: var(--space-4) 0;
}

.stack {
    display: flex;
    flex-direction: column;
    gap: var(--space-3);
}
</style>