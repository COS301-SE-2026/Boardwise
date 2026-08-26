<template>
    <PageContainer data-test="page-container">
        <Navbar data-test="navbar" />

        <v-container v-if="loading" class="d-flex justify-center align-center" style="min-height: 60vh">
            <v-progress-circular data-test="loading-spinner" indeterminate color="primary" size="48" />
        </v-container>

       <v-container v-else-if="!query" class="d-flex flex-column align-center justify-center" style="min-height: 60vh">
        <p class="text-medium-emphasis">Start typing to search games, users, and rules.</p>
       </v-container>

        <SearchResults 
            v-else
            data-test="search-results"
            :query="query"
            :people="people"
            :rulebooks="rulebooks"
            :listings="listings"
            :communities="communities"
            @friend-action="handleFriendACtion"
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

import { useRouter, useRoute } from 'vue-router'
import { useDebounceFn } from '@vueuse/core'
import { useSearch } from '~/composables/useSearch'

const router = useRouter()
const route = useRoute()
const query = computed(() => route.query.q ?? '')

const { people, rulebooks, listings, communities, loading, search } = useSearch()

const delaySearch = useDebounceFn((q) => search(q), 400)

watch(query, (q) => delaySearch(q), { immediate: true })

function handleOpenRulebook(rb) {
    router.push(`/library/${rb.id}`)
}

function handleOpenListing(listing) {
    router.push(`/marketplace/${listing.id}`)
}

function handleOpenProfile(person) {
    router.push(`/profile/${person.id}`)
}

function handleFriendAction(person) {
    // TODO: wire to friend/social service once it exists
  console.log('friend action', person)
}

</script>