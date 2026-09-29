<template>
    <PageContainer data-test="page-container">
        <Navbar data-test="navbar" />

        <BaseLoadingState v-if="loading" />

       <BaseEmptyState
            v-else-if="!query"
            title="Search Boardwise"
            message="Start typing to search games, users, and rules."
        />

        <SearchResults 
            v-else
            data-test="search-results"
            :query="query"
            :people="people"
            :rulebooks="rulebooks"
            :listings="listings"
            :communities="communities"
            @friend-action="handleFriendAction"
            @open-profile="handleOpenProfile"
            @open-rulebook="handleOpenRulebook"
            @open-listing="handleOpenListing"
        />
        
    </PageContainer>
</template>

<script setup>
definePageMeta({
  middleware: 'auth'
})

import SearchResults from '~/components/features/search/SearchResults.vue'
import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'
import BaseLoadingState from '~/components/ui/BaseLoadingState.vue'
import BaseEmptyState from '~/components/ui/BaseEmptyState.vue'

import { watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useSearch } from '~/composables/useSearch'
import { useFriends } from '~/composables/useFriends'
import { useDebouncedAutocomplete } from '~/composables/useDebounce'

const { sendFriendRequest } = useFriends();

const router = useRouter()
const route = useRoute()

const { people, rulebooks, listings, communities, loading, search } = useSearch()

const fetchSearchData = async (query) => {
    await search(query);
    return []
}

const {search:searchQuery} = useDebouncedAutocomplete(fetchSearchData, {debounceMs: 400, fetchOnMount: false});

watch(() => route.query.q , (newQuery) => {
    searchQuery.value = newQuery ?? ''
}, { immediate: true })

function handleOpenRulebook(rb) {
    router.push(`/library/${rb.id}`)
}

function handleOpenListing(listing) {
    router.push(`/marketplace/${listing.id}`)
}

function handleOpenProfile(person) {
    router.push(`/profile/${person.id}`)
}

const handleFriendAction =  async (person) => {
    person.isFriend = true
    return await sendFriendRequest(person.id);
}
</script>